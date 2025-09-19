package com.db.assetstore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import com.db.assetstore.AssetType;
import com.db.assetstore.model.Asset;
import com.db.assetstore.model.AttributeValue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Responsible for creating Asset model objects from JSON payloads.
 * Keeps all JSON-related parsing isolated from service/repository layers.
 */
@Slf4j
public class AssetJsonFactory {
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_ID = "id";
    private static final String FIELD_ATTRIBUTES = "attributes";

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Parse a type-specific JSON (without generic wrapper), validate against schema, and build Asset.
     * The JSON is expected to be a flat object where all fields except 'id' are treated as attributes.
     * Schema is loaded from classpath at schemas/{type}.schema.json.
     */
    public Asset fromJsonForType(AssetType type, String json) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(json, "json");
        // validate using minimal schema validator
        String schemaPath = "schemas/" + type.name() + ".schema.json";
        JsonSchemaValidator.validateOrThrow(json, schemaPath);
        try {
            JsonNode root = parseJson(json);
            String id = optText(root, FIELD_ID);
            if (id == null || id.isBlank()) {
                id = java.util.UUID.randomUUID().toString();
            }
            List<AttributeValue<?>> attrs = new ArrayList<>();
            Iterator<String> fieldNames = root.fieldNames();
            while (fieldNames.hasNext()) {
                String name = fieldNames.next();
                if (FIELD_ID.equals(name)) continue;
                JsonNode valueNode = root.get(name);
                attrs.add(toAttributeValue(name, valueNode));
            }
            return new Asset(id, type, java.time.Instant.now(), attrs);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid JSON payload: " + ex.getMessage(), ex);
        }
    }

    /**
     * Parse JSON like: {"type":"CRE","id":"optional-uuid","attributes":{"city":"Warsaw","area":100.0}}
     * Returns a fully constructed Asset domain object. Throws IllegalArgumentException on invalid input.
     */
    public Asset fromJson(String json) {
        Objects.requireNonNull(json, "json");
        try {
            JsonNode root = parseJson(json);
            AssetType type = readType(root);
            String id = readId(root);
            List<AttributeValue<?>> attrs = readAttributes(root.get(FIELD_ATTRIBUTES));

            Asset asset = new Asset(id, type, Instant.now(), attrs);
            log.debug("Created Asset from JSON: type={}, id={}, attrCount={}", type, id, attrs.size());
            return asset;
        } catch (IllegalArgumentException ex) {
            // Validation / domain errors: rethrow as-is after concise log
            log.warn("Asset JSON validation error: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            // Any other parsing problem -> wrap into IllegalArgumentException as API contract
            log.error("Failed to parse asset JSON", ex);
            throw new IllegalArgumentException("Invalid JSON payload: " + ex.getMessage(), ex);
        }
    }

    // --- small, focused helpers for readability ---

    private JsonNode parseJson(String json) throws Exception {
        return mapper.readTree(json);
    }

    private AssetType readType(JsonNode root) {
        String typeStr = optText(root, FIELD_TYPE);
        if (typeStr == null) throw new IllegalArgumentException("Missing 'type' in JSON payload");
        try {
            return AssetType.valueOf(typeStr);
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("Unknown asset type: " + typeStr);
        }
    }

    private String readId(JsonNode root) {
        String id = optText(root, FIELD_ID);
        return (id == null || id.isBlank()) ? java.util.UUID.randomUUID().toString() : id;
    }

    private List<AttributeValue<?>> readAttributes(JsonNode attrsNode) {
        List<AttributeValue<?>> attrs = new ArrayList<>();
        if (attrsNode == null || !attrsNode.isObject()) {
            return attrs; // empty list
        }

        Iterator<String> names = attrsNode.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            JsonNode valueNode = attrsNode.get(name);
            attrs.add(toAttributeValue(name, valueNode));
        }
        return attrs;
    }

    private static String optText(JsonNode node, String field) {
        JsonNode f = node != null ? node.get(field) : null;
        return f != null && !f.isNull() ? f.asText() : null;
    }

    private static AttributeValue<?> toAttributeValue(String name, JsonNode node) {
        if (node == null || node.isNull()) {
            return attr(name, null, String.class); // keep nulls typed as String for compatibility
        }
        switch (node.getNodeType()) {
            case STRING:
                return attr(name, node.asText(), String.class);
            case NUMBER:
                if (node.isIntegralNumber()) {
                    return attr(name, node.asLong(), Long.class);
                } else { // floating point (float/double/decimal)
                    return attr(name, node.asDouble(), Double.class);
                }
            case BOOLEAN:
                return attr(name, node.asBoolean(), Boolean.class);
            default:
                // For arrays/objects and other types, fall back to String representation
                return attr(name, node.asText(), String.class);
        }
    }

    private static AttributeValue<?> attr(String name, Object value, Class<?> type) {
        @SuppressWarnings({"rawtypes", "unchecked"})
        AttributeValue<?> av = new AttributeValue(name, value, (Class) type);
        return av;
    }
}
