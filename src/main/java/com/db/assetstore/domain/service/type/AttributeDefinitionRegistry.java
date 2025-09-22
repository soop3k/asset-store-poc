package com.db.assetstore.domain.service.type;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.schema.TypeSchemaRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builds attribute definitions for each supported AssetType based on the JSON Schemas
 * present on the classpath. This registry is intended to support downstream components
 * (e.g., persistence layer) that need a catalog of known attributes and which ones are required.
 *
 * Change: store attribute type as an abstraction (ValueType) instead of plain String names.
 */
public final class AttributeDefinitionRegistry {
    private static final Logger log = LoggerFactory.getLogger(AttributeDefinitionRegistry.class);
    private static final ObjectMapper M = new ObjectMapper();
    private static final AttributeDefinitionRegistry INSTANCE = new AttributeDefinitionRegistry();

    public static AttributeDefinitionRegistry getInstance() { return INSTANCE; }

    private final Map<AssetType, Map<String, Def>> defsByType = new ConcurrentHashMap<>();

    private AttributeDefinitionRegistry() {
        rebuild();
    }

    public Map<String, Def> getDefinitions(AssetType type) {
        return defsByType.getOrDefault(type, Collections.emptyMap());
    }


    private void rebuild() {
        TypeSchemaRegistry reg = TypeSchemaRegistry.getInstance();
        for (AssetType t : reg.supportedTypes()) {
            String path = reg.getSchemaPath(t).orElse(null);
            if (path == null) {
                continue;
            }
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
                if (is == null) {
                    continue;
                }
                JsonNode schema = M.readTree(is);
                Map<String, Def> defs = buildDefsFromSchema(t, schema);
                defsByType.put(t, defs);
                long reqCount = defs.values().stream().filter(Def::required).count();
                log.info("AttributeDefinitionRegistry: type={} defs={} (required={})", t, defs.size(), reqCount);
            } catch (Exception e) {
                log.warn("Failed to load schema for type {} from {}: {}", t, path, e.getMessage());
            }
        }
    }

    private static Map<String, Def> buildDefsFromSchema(AssetType type, JsonNode schema) {
        Map<String, Def> map = new LinkedHashMap<>();
        if (schema == null || !schema.isObject()) {
            return map;
        }
        JsonNode props = schema.get("properties");
        Set<String> required = new HashSet<>();
        JsonNode req = schema.get("required");
        if (req != null && req.isArray()) {
            req.forEach(n -> {
                if (n.isTextual()) {
                    required.add(n.asText());
                }
            });
        }
        if (props != null && props.isObject()) {
            Iterator<String> names = props.fieldNames();
            while (names.hasNext()) {
                String name = names.next();
                JsonNode def = props.get(name);
                ValueType vt = readValueType(def);
                boolean reqd = required.contains(name);
                map.put(name, new Def(name, vt, reqd));
            }
        }
        return map;
    }

    private static ValueType readValueType(JsonNode def) {
        if (def == null || !def.isObject()) {
            return ValueType.STRING;
        }
        JsonNode t = def.get("type");
        if (t == null) {
            return ValueType.STRING;
        }
        String s = t.asText();
        return switch (s) {
            case "integer" -> ValueType.DECIMAL;
            case "number" -> ValueType.DECIMAL;
            case "boolean" -> ValueType.BOOLEAN;
            case "string" -> ValueType.STRING;
            default -> ValueType.STRING;
        };
    }

    /**
     * Abstract value type instead of plain strings; provides a DB-friendly name when needed.
     */
    public enum ValueType {
        STRING("String"),
        DECIMAL("Decimal"),
        BOOLEAN("Boolean");

        private final String dbName;
        ValueType(String dbName) { this.dbName = dbName; }
        public String dbName() { return dbName; }
        public Class<?> javaClass() {
            return switch (this) {
                case STRING -> String.class;
                case DECIMAL -> java.math.BigDecimal.class;
                case BOOLEAN -> Boolean.class;
            };
        }
    }

    public record Def(String name, ValueType valueType, boolean required) {
        public boolean required() { return required; }
    }
}
