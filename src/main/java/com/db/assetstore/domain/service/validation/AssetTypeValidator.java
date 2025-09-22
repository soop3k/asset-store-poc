package com.db.assetstore.domain.service.validation;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.AttributeValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.db.assetstore.infra.config.JsonMapperProvider;
import com.db.assetstore.domain.schema.TypeSchemaRegistry;

import java.util.*;

/**
 * Centralizes validation related to AssetType and its JSON Schema.
 * Wraps TypeSchemaRegistry and JsonSchemaValidator to keep higher-level services slim.
 */
public final class AssetTypeValidator {
    private final TypeSchemaRegistry registry = TypeSchemaRegistry.getInstance();
    private static final ObjectMapper MAPPER = JsonMapperProvider.get();

    public void ensureSupported(AssetType type) {
        Objects.requireNonNull(type, "type");
        // Require schema presence to treat type as supported
        Optional<String> schema = registry.getSchemaPath(type);
        if (schema.isEmpty()) {
            throw new IllegalArgumentException("Unsupported asset type: " + type + " - schema not found");
        }
    }

    public void validateAttributes(AssetType type, Collection<AttributeValue<?>> attrs) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(attrs, "attrs");
        String schemaPath = registry.getSchemaPath(type).orElse(null);
        // Build a simple JSON object from attribute list to validate against schema
        Map<String, Object> map = new LinkedHashMap<>();
        for (AttributeValue<?> av : attrs) {
            if (av == null) continue;
            map.put(av.name(), av.value());
        }
        try {
            String json = MAPPER.writeValueAsString(map);
            JsonSchemaValidator.validateIfPresent(json, schemaPath);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to encode attributes JSON: " + e.getMessage(), e);
        }
    }
}
