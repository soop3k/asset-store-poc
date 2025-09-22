package com.db.assetstore.domain.json;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.db.assetstore.domain.service.type.AttributeDefinitionRegistry.ValueType;

@Component
@RequiredArgsConstructor
public final class AttributeJsonReader {

    private final ObjectMapper mapper;

    public List<AttributeValue<?>> read(AssetType type, JsonNode obj) {
        if (type == null || obj == null || !obj.isObject()) return List.of();

        var defs = AttributeDefinitionRegistry.getInstance().getDefinitions(type);
        if (defs == null || defs.isEmpty()) return List.of();

        var converted = new ArrayList<AttributeValue<?>>();
        for (var e : defs.entrySet()) {
            String name = e.getKey();
            if (!obj.has(name)) continue;

            JsonNode node = obj.get(name);
            ValueType vt = e.getValue().valueType();
            if (node.isArray()) {
                for (JsonNode item : node) {
                    converted.add(createAV(name, vt, convert(item, vt)));
                }
            } else {
                converted.add(createAV(name, vt, convert(node, vt)));
            }
        }
        return converted;
    }

    private Object convert(JsonNode node, ValueType vt) {
        if (node == null || node.isNull()) return null;
        try {
            return switch (vt == null ? ValueType.STRING : vt) {
                case STRING  -> mapper.convertValue(node, String.class);
                case DECIMAL -> mapper.convertValue(node, BigDecimal.class);
                case BOOLEAN -> mapper.convertValue(node, Boolean.class);
            };
        } catch (IllegalArgumentException ex) {
            String got = node.isTextual() ? "'" + node.asText() + "'" : node.getNodeType().name();
            throw new IllegalArgumentException("Attribute expected " + vt + " but got " + got, ex);
        }
    }

    private AttributeValue<?> createAV(String name, ValueType vt, Object value) {
        return switch (vt == null ? ValueType.STRING : vt) {
            case STRING  -> new AVString(name, (String) value);
            case DECIMAL -> new AVDecimal(name, (BigDecimal) value);
            case BOOLEAN -> new AVBoolean(name, (Boolean) value);
        };
    }
}
