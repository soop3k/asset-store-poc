package com.db.assetstore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;

/**
 * JSON Schema validation facade.
 * - If the networknt json-schema-validator library is present on the classpath, use it.
 * - Otherwise, fall back to a minimal built-in validator for simple cases.
 */
public final class JsonSchemaValidator {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonSchemaValidator() {}

    public static void validateOrThrow(String jsonPayload, String schemaClasspathResource) {
        try {
            if (schemaClasspathResource == null) return; // no schema -> skip
            InputStream is = JsonSchemaValidator.class.getClassLoader().getResourceAsStream(schemaClasspathResource);
            if (is == null) {
                throw new IllegalArgumentException("Schema not found: " + schemaClasspathResource);
            }
            JsonNode schema = MAPPER.readTree(is);
            JsonNode payload = MAPPER.readTree(jsonPayload);

            if (!tryNetworknt(schema, payload)) {
                // Fallback to minimal in-house validator
                List<String> errors = minimalValidate(schema, payload);
                if (!errors.isEmpty()) {
                    throw new IllegalArgumentException("JSON schema validation failed: " + String.join("; ", errors));
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to validate JSON against schema: " + e.getMessage(), e);
        }
    }

    // Attempt to validate using networknt via reflection to avoid compile-time dependency
    private static boolean tryNetworknt(JsonNode schemaNode, JsonNode payloadNode) {
        try {
            Class<?> factoryCls = Class.forName("com.networknt.schema.JsonSchemaFactory");
            Class<?> specFlagCls = Class.forName("com.networknt.schema.SpecVersion$VersionFlag");
            Class<?> jsonSchemaCls = Class.forName("com.networknt.schema.JsonSchema");
            Class<?> validationMessageCls = Class.forName("com.networknt.schema.ValidationMessage");

            // JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)
            Method getInstance = factoryCls.getMethod("getInstance", specFlagCls);
            Object v7 = Enum.valueOf((Class<Enum>) specFlagCls, "V7");
            Object factory = getInstance.invoke(null, v7);

            // factory.getSchema(JsonNode)
            Method getSchema = factoryCls.getMethod("getSchema", JsonNode.class);
            Object schema = getSchema.invoke(factory, schemaNode);

            // schema.validate(JsonNode) -> Set<ValidationMessage>
            Method validate = jsonSchemaCls.getMethod("validate", JsonNode.class);
            Object result = validate.invoke(schema, payloadNode);

            if (result instanceof java.util.Set<?> set) {
                if (!set.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (Object vm : set) {
                        Method getMessage = validationMessageCls.getMethod("getMessage");
                        String msg = (String) getMessage.invoke(vm);
                        if (sb.length() > 0) sb.append("; ");
                        sb.append(msg);
                    }
                    throw new IllegalArgumentException("JSON schema validation failed: " + sb);
                }
            }
            return true; // validated successfully
        } catch (ClassNotFoundException cnf) {
            return false; // library not present
        } catch (IllegalArgumentException iae) {
            throw iae; // rethrow validation failure
        } catch (Exception e) {
            // Any other reflection issue -> treat as not available, fall back
            return false;
        }
    }

    // Minimal fallback validator (same as previous in-house implementation, limited feature set)
    private static List<String> minimalValidate(JsonNode schema, JsonNode data) {
        List<String> errs = new ArrayList<>();
        if (schema == null || data == null) return errs;
        String type = text(schema, "type");
        if (type != null && !"object".equals(type)) {
            errs.add("Unsupported schema type: " + type);
            return errs;
        }
        if (!data.isObject()) {
            errs.add("Payload must be a JSON object");
            return errs;
        }
        // required
        JsonNode required = schema.get("required");
        if (required != null && required.isArray()) {
            for (JsonNode r : required) {
                String name = r.asText();
                if (!data.has(name) || data.get(name).isNull()) {
                    errs.add("Missing required field: " + name);
                }
            }
        }
        // properties type checks
        JsonNode props = schema.get("properties");
        if (props != null && props.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = props.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> e = fields.next();
                String name = e.getKey();
                String pt = text(e.getValue(), "type");
                if (pt == null) continue;
                if (!data.has(name) || data.get(name).isNull()) continue; // optional or null allowed
                JsonNode v = data.get(name);
                switch (pt) {
                    case "string" -> { if (!v.isTextual()) errs.add(typeErr(name, pt, v)); }
                    case "number" -> { if (!v.isNumber()) errs.add(typeErr(name, pt, v)); }
                    case "integer" -> { if (!v.isIntegralNumber()) errs.add(typeErr(name, pt, v)); }
                    case "boolean" -> { if (!v.isBoolean()) errs.add(typeErr(name, pt, v)); }
                    case "object" -> { if (!v.isObject()) errs.add(typeErr(name, pt, v)); }
                    case "array" -> { if (!v.isArray()) errs.add(typeErr(name, pt, v)); }
                    default -> errs.add("Unsupported property type: " + pt + " for field " + name);
                }
            }
        }
        return errs;
    }

    private static String text(JsonNode n, String field) {
        JsonNode x = n != null ? n.get(field) : null;
        return x != null && !x.isNull() ? x.asText() : null;
    }

    private static String typeErr(String name, String expected, JsonNode actual) {
        String at = actual.isNull() ? "null" : actual.getNodeType().toString().toLowerCase();
        return "Field '" + name + "' should be " + expected + " but was " + at;
    }

    public static void validateIfPresent(String jsonPayload, String schemaClasspathResource) {
        try {
            if (schemaClasspathResource == null) return;
            java.io.InputStream is = JsonSchemaValidator.class.getClassLoader().getResourceAsStream(schemaClasspathResource);
            if (is == null) return; // nothing to validate against
        } catch (Exception ignored) {
            return;
        }
        validateOrThrow(jsonPayload, schemaClasspathResource);
    }
}