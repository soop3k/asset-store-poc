package com.db.assetstore.domain.json;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.math.BigDecimal;

public final class AssetAttributeConverter {

    public List<AttributeValue<?>> readAttributes(AssetType type, JsonNode attrsNode) {
        if (type == null || attrsNode == null || !attrsNode.isObject()) return List.of();

        Map<String, AttributeDefinitionRegistry.Def> defs =
                AttributeDefinitionRegistry.getInstance().getDefinitions(type);

        List<AttributeValue<?>> out = new ArrayList<>();
        if (defs != null && !defs.isEmpty()) {
            for (var e : defs.entrySet()) {
                String name = e.getKey();
                if (!attrsNode.has(name)) continue;
                out.add(toAttributeValue(name, attrsNode.get(name), e.getValue().valueType()));
            }
            return out;
        }
        // Fallback when no definitions are available: infer types from JSON nodes
        attrsNode.fields().forEachRemaining(entry -> {
            String name = entry.getKey();
            JsonNode node = entry.getValue();
            AttributeDefinitionRegistry.ValueType vt;
            if (node == null || node.isNull()) {
                // default to string for nulls
                vt = AttributeDefinitionRegistry.ValueType.STRING;
            } else if (node.isBoolean()) {
                vt = AttributeDefinitionRegistry.ValueType.BOOLEAN;
            } else if (node.isNumber()) {
                vt = AttributeDefinitionRegistry.ValueType.DECIMAL;
            } else {
                vt = AttributeDefinitionRegistry.ValueType.STRING;
            }
            out.add(toAttributeValue(name, node, vt));
        });
        return out;
    }

    public AttributeValue<?> toAttributeValue(String name, JsonNode node,
                                              AttributeDefinitionRegistry.ValueType target) {
        if (target == null) {
            target = AttributeDefinitionRegistry.ValueType.STRING;
        }

        return switch (target) {
            case STRING  -> new AVString(name, asString(node));
            case DECIMAL -> new AVDecimal(name, asDecimal(name, node));
            case BOOLEAN -> new AVBoolean(name, asBoolean(name, node));
        };
    }

    private static String asString(JsonNode n) {
        return (n == null || n.isNull()) ? null : n.asText();
    }

    private static BigDecimal asDecimal(String name, JsonNode n) {
        if (n == null || n.isNull()) return null;
        if (n.isNumber()) return n.decimalValue();
        if (n.isTextual()) {
            String s = n.asText().trim();
            try { return new BigDecimal(s); }
            catch (NumberFormatException ex) {
                throw new IllegalArgumentException(err(name, "DECIMAL", s), ex);
            }
        }
        throw new IllegalArgumentException(err(name, "DECIMAL", n.getNodeType().name()));
    }

    private static Boolean asBoolean(String name, JsonNode n) {
        if (n == null || n.isNull()) return null;
        if (n.isBoolean()) return n.asBoolean();
        if (n.isTextual()) {
            String s = n.asText().trim().toLowerCase(Locale.ROOT);
            if ("true".equals(s) || "false".equals(s)) return Boolean.valueOf(s);
            throw new IllegalArgumentException(err(name, "BOOLEAN", s));
        }
        throw new IllegalArgumentException(err(name, "BOOLEAN", n.getNodeType().name()));
    }

    private static String err(String name, String expected, String got) {
        return "Attribute '" + name + "' expected " + expected + " but got '" + got + "'";
    }
}
