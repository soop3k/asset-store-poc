package com.db.assetstore.domain.json;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.AttributeValue;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Locale;

/**
 * Converts JSON attribute objects to typed AttributeValue list.
 * Extracted to reduce complexity of AssetJsonFactory.
 */
public final class AssetAttributeConverter {

    /**
     * New: Conversion that leverages attribute definitions (if present for the type) to coerce values.
     * If no definition for an attribute is found, falls back to the lenient conversion above.
     */
    public List<AttributeValue<?>> readAttributes(AssetType type, JsonNode attrsNode) {
        List<AttributeValue<?>> attrs = new ArrayList<>();
        if (attrsNode == null || !attrsNode.isObject()) {
            return attrs; // empty list
        }
        Map<String, AttributeDefinitionRegistry.Def> defs = type != null
                ? AttributeDefinitionRegistry.getInstance().getDefinitions(type)
                : Collections.emptyMap();
        if (defs != null && !defs.isEmpty()) {
            // Iterate over known definitions only: pick values from JSON if present and coerce per definition
            for (Map.Entry<String, AttributeDefinitionRegistry.Def> e : defs.entrySet()) {
                String name = e.getKey();
                if (attrsNode.has(name)) {
                    JsonNode valueNode = attrsNode.get(name);
                    attrs.add(toAttributeValue(name, valueNode, e.getValue().valueType()));
                }
            }
            // Do not include attributes not covered by definitions (no fallbacks)
            return attrs;
        }
        // No definitions available -> do not parse any attributes (strict mode, no fallback)
        return attrs;
    }

    public AttributeValue<?> toAttributeValue(String name, JsonNode node) {
        if (node == null || node.isNull()) {
            return attr(name, null, String.class);
        }
        switch (node.getNodeType()) {
            case STRING:
                return attr(name, node.asText(), String.class);
            case NUMBER:
                return attr(name, node.decimalValue(), java.math.BigDecimal.class);
            case BOOLEAN:
                return attr(name, node.asBoolean(), Boolean.class);
            default:
                return attr(name, node.asText(), String.class);
        }
    }

    /**
     * Convert using a target type derived from schema definitions.
     * The conversion is permissive: textual numbers/booleans are parsed when possible.
     */
    public AttributeValue<?> toAttributeValue(String name, JsonNode node, AttributeDefinitionRegistry.ValueType target) {
        if (node == null || node.isNull()) {
            // keep null with declared target type for downstream typing consistency
            Class<?> t = toJavaClass(target);
            return attr(name, null, t == null ? String.class : t);
        }
        if (target == null) {
            return attr(name, node.asText(), String.class);
        }
        switch (target) {
            case DECIMAL:
                if (node.isNumber()) {
                    return attr(name, node.decimalValue(), java.math.BigDecimal.class);
                }
                if (node.isTextual()) {
                    String s = node.asText().trim();
                    try {
                        return attr(name, new java.math.BigDecimal(s), java.math.BigDecimal.class);
                    } catch (NumberFormatException nfe) {
                        throw new IllegalArgumentException("Attribute '" + name + "' expected DECIMAL but got '" + s + "'");
                    }
                }
                throw new IllegalArgumentException("Attribute '" + name + "' expected DECIMAL but got non-numeric JSON type");
            case BOOLEAN:
                if (node.isBoolean()) {
                    return attr(name, node.asBoolean(), Boolean.class);
                }
                if (node.isTextual()) {
                    String s = node.asText().trim().toLowerCase(Locale.ROOT);
                    if ("true".equals(s) || "false".equals(s)) {
                        return attr(name, Boolean.valueOf(s), Boolean.class);
                    }
                    throw new IllegalArgumentException("Attribute '" + name + "' expected BOOLEAN but got '" + s + "'");
                }
                throw new IllegalArgumentException("Attribute '" + name + "' expected BOOLEAN but got non-boolean JSON type");
            case STRING:
            default:
                // always stringify
                return attr(name, node.asText(), String.class);
        }
    }

    private static Class<?> toJavaClass(AttributeDefinitionRegistry.ValueType vt) {
        if (vt == null) {
            return String.class;
        }
        return vt.javaClass();
    }

    private static AttributeValue<?> attr(String name, Object value, Class<?> type) {
        @SuppressWarnings({"rawtypes", "unchecked"})
        AttributeValue<?> av = new AttributeValue(name, value, (Class) type);
        return av;
    }
}
