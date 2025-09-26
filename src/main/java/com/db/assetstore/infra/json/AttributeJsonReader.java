package com.db.assetstore.infra.json;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.model.type.AttributeType;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public final class AttributeJsonReader {

    private final ObjectMapper mapper;
    private final AttributeDefinitionRegistry attributeDefinitionRegistry;

    public List<AttributeValue<?>> read(AssetType type, JsonNode obj) {
        if (type == null || obj == null || !obj.isObject()) {
            return List.of();
        }

        var defs = attributeDefinitionRegistry.getDefinitions(type);
        if (defs == null || defs.isEmpty()) {
            return List.of();
        }

        var converted = new ArrayList<AttributeValue<?>>();
        for (var entry : defs.entrySet()) {
            String name = entry.getKey();
            if (!obj.has(name)) {
                continue;
            }

            JsonNode node = obj.get(name);
            AttributeType attributeType = entry.getValue().attributeType();
            if (node.isArray()) {
                for (JsonNode item : node) {
                    converted.add(createAV(name, attributeType, convert(item, attributeType)));
                }
            } else {
                converted.add(createAV(name, attributeType, convert(node, attributeType)));
            }
        }
        return converted;
    }

    private Object convert(JsonNode node, AttributeType attributeType) {
        if (node == null || node.isNull()) {
            return null;
        }
        try {
            AttributeType effectiveType = attributeType == null ? AttributeType.STRING : attributeType;
            return switch (effectiveType) {
                case STRING  -> mapper.convertValue(node, String.class);
                case DECIMAL -> mapper.convertValue(node, BigDecimal.class);
                case BOOLEAN -> mapper.convertValue(node, Boolean.class);
            };
        } catch (IllegalArgumentException ex) {
            String got = node.isTextual() ? "'" + node.asText() + "'" : node.getNodeType().name();
            throw new IllegalArgumentException("Attribute expected " + attributeType + " but got " + got, ex);
        }
    }

    private AttributeValue<?> createAV(String name, AttributeType attributeType, Object value) {
        AttributeType effectiveType = attributeType == null ? AttributeType.STRING : attributeType;
        return switch (effectiveType) {
            case STRING  -> new AVString(name, (String) value);
            case DECIMAL -> new AVDecimal(name, (BigDecimal) value);
            case BOOLEAN -> new AVBoolean(name, (Boolean) value);
        };
    }
}
