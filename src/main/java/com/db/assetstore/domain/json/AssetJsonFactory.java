package com.db.assetstore.domain.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
// Removed infra dependency: use local ObjectMapper
import lombok.extern.slf4j.Slf4j;
import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Map;

/**
 * Responsible for creating Asset model objects from JSON payloads.
 * Keeps all JSON-related parsing isolated from service/repository layers.
 * Does NOT perform type or schema validation (DDD separation) – callers must validate beforehand.
 */
@Slf4j
public class AssetJsonFactory {
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_ID = "id";

    private final ObjectMapper mapper;
    private final AssetAttributeConverter attributeConverter = new AssetAttributeConverter();

    public AssetJsonFactory() {
        this.mapper = new ObjectMapper();
    }


    /**
     * Parse a type-specific JSON (without generic wrapper) and build Asset.
     * The JSON is expected to be a flat object where all fields except 'id' are treated as attributes.
     * No validation is performed here.
     */
    public Asset fromJsonForType(AssetType type, String json) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(json, "json");
        try {
            JsonNode root = parseJson(json);
            return buildAsset(root, type);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid JSON payload: " + ex.getMessage(), ex);
        }
    }

    /**
     * Parse flat JSON like: {"type":"CRE","id":"uuid","city":"Warsaw","area":100.0}
     * Returns a fully constructed Asset domain object. Throws IllegalArgumentException on invalid input.
     * No validation is performed here; callers are responsible for validating type and attributes.
     */
    public Asset fromJson(String json) {
        Objects.requireNonNull(json, "json");
        try {
            JsonNode root = parseJson(json);
            return buildAsset(root, null);
        } catch (IllegalArgumentException ex) {
            log.warn("Asset JSON error: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to parse asset JSON", ex);
            throw new IllegalArgumentException("Invalid JSON payload: " + ex.getMessage(), ex);
        }
    }

    /**
     * Same as fromJson(String), but accepts a pre-parsed JsonNode (object) for bulk parsing flows.
     */
    public Asset fromJson(JsonNode root) {
        Objects.requireNonNull(root, "root");
        if (!root.isObject()) {
            throw new IllegalArgumentException("Expected JSON object for asset, got: " + root.getNodeType());
        }
        try {
            return buildAsset(root, null);
        } catch (IllegalArgumentException ex) {
            log.warn("Asset JSON error: {}", ex.getMessage());
            throw ex;
        }
    }

    // --- small, focused helpers for readability ---

    private JsonNode parseJson(String json) throws Exception {
        return mapper.readTree(json);
    }

    private AssetType readType(JsonNode root) {
        String typeStr = optText(root, FIELD_TYPE);
        if (typeStr == null) {
            throw new IllegalArgumentException("Missing 'type' in JSON payload");
        }
        try {
            return AssetType.valueOf(typeStr);
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("Unknown asset type: " + typeStr);
        }
    }

    private String readId(JsonNode root) {
        String id = optText(root, FIELD_ID);
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Missing 'id' in JSON payload");
        }
        return id;
    }

    private static String optText(JsonNode node, String field) {
        JsonNode f = node != null ? node.get(field) : null;
        return f != null && !f.isNull() ? f.asText() : null;
    }



    private ObjectNode collectAttributesFromTypeRoot(JsonNode root, AssetType type) {
        ObjectNode attrsNode = mapper.createObjectNode();
        if (root != null && root.isObject() && type != null) {
            Map<String, AttributeDefinitionRegistry.Def> defs =
                    AttributeDefinitionRegistry.getInstance().getDefinitions(type);
            if (defs != null && !defs.isEmpty()) {
                for (String name : defs.keySet()) {
                    if (FIELD_ID.equals(name)) {
                        continue; // 'id' is a core field, not an attribute
                    }
                    JsonNode val = root.get(name);
                    if (val != null) {
                        attrsNode.set(name, val);
                    }
                }
            } else {
                // Fallback: if no schema-defined attributes found, collect all fields except core ones
                root.fields().forEachRemaining(e -> {
                    String name = e.getKey();
                    if (!FIELD_ID.equals(name) && !FIELD_TYPE.equals(name)) {
                        attrsNode.set(name, e.getValue());
                    }
                });
            }
        }
        return attrsNode;
    }

    // Unified builder used by both fromJson and fromJsonForType
    private Asset buildAsset(JsonNode root, AssetType providedType) {
        AssetType type = providedType != null ? providedType : readType(root);
        String id = readId(root);
        Asset asset = Asset.builder()
                .id(id)
                .type(type)
                .createdAt(Instant.now())
                .build();
        try {
            mapper.readerForUpdating(asset).readValue(root);
        } catch (Exception ex) {
            if (log.isDebugEnabled()) {
                log.debug("Jackson update of Asset failed: {}", ex.getMessage());
            }
        }
        // Attributes: pick only fields defined for the type from the flat JSON root.
        // Use providedType only as a hint for which definitions to load; prefer the JSON-declared type.
        AssetType defsType = asset.getType() != null ? asset.getType() : providedType;
        ObjectNode attrsObject = collectAttributesFromTypeRoot(root, defsType);
        List<AttributeValue<?>> attrs = attributeConverter.readAttributes(defsType, attrsObject);
        asset.setAttributes(attrs);
        log.debug("Created Asset from JSON: type={}, id={}, attrCount={}", asset.getType(), asset.getId(), attrs.size());
        return asset;
    }

}
