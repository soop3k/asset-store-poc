package com.db.assetstore.domain.service.validation;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.service.type.TypeSchemaRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public final class AssetTypeValidator {

    private final JsonSchemaValidator validator;
    private final TypeSchemaRegistry registry;
    private final ObjectMapper objectMapper;

    public void ensureSupported(@NonNull AssetType type) {
        Optional<String> schema = registry.getSchemaPath(type);
        if (schema.isEmpty()) {
            throw new IllegalArgumentException("Unsupported asset type: " + type + " - schema not found");
        }
    }

    public void validateAttributes(@NonNull AssetType type, @NonNull Collection<AttributeValue<?>> attrs) {
        String schemaPath = registry.getSchemaPath(type).orElse(null);
        Map<String, Object> map = new LinkedHashMap<>();
        for (AttributeValue<?> av : attrs) {
            if (av == null) {
                continue;
            }
            map.put(av.name(), av.value());
        }
        try {
            String json = objectMapper.writeValueAsString(map);
            validator.validateOrThrow(json, schemaPath);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to encode attributes JSON: " + e.getMessage(), e);
        }
    }
}
