package com.db.assetstore.domain.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
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
@Component
@RequiredArgsConstructor
public class AssetJsonFactory {
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_ID = "id";

    private final ObjectMapper mapper;
    private final AttributeJsonReader jsonReader;
    private final AttributeDefinitionRegistry attributeDefinitionRegistry;

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
        } catch (Exception ex) {
            log.error("Failed to parse asset JSON", ex);
            throw new IllegalArgumentException("Invalid JSON payload: " + ex.getMessage(), ex);
        }
    }

    public Asset fromJson(JsonNode root) {
        Objects.requireNonNull(root, "root");
        if (!root.isObject()) {
            throw new IllegalArgumentException("Expected JSON object for asset, got: " + root.getNodeType());
        }

        return buildAsset(root, null);
    }

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

    private ObjectNode collectAttributes(JsonNode root, AssetType type) {
        final ObjectNode attributes = mapper.createObjectNode();

        if (root == null || !root.isObject() || type == null) {
            return attributes;
        }

        final var definitions = attributeDefinitionRegistry.getDefinitions(type);

        if (definitions.isEmpty()) {
            return attributes;
        }

        for (String attrName : definitions.keySet()) {
            final JsonNode value = root.get(attrName);
            if (value != null) {
                attributes.set(attrName, value);
            }
        }

        return attributes;
    }

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
        } catch (IllegalArgumentException ex) {
            log.warn("Asset JSON error: {}", ex.getMessage());
        } catch (Exception ex) {
            if (log.isDebugEnabled()) {
                log.debug("Update of Asset failed: {}", ex.getMessage());
            }
        }

        // Attributes: pick only fields defined for the type from the flat JSON root.
        // Use providedType only as a hint for which definitions to load; prefer the JSON-declared type.
        var attrsObject = collectAttributes(root, type);
        asset.setAttributes(jsonReader.read(type, attrsObject));

        log.debug("Created Asset from JSON: type={}, id={}", asset.getType(), asset.getId());
        return asset;
    }

}
