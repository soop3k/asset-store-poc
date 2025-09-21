package com.db.assetstore.service.transform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schibsted.spt.data.jslt.Expression;
import com.schibsted.spt.data.jslt.Parser;
import com.db.assetstore.service.validation.JsonSchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JSON-to-JSON transformer using JSLT (Schibsted) templates via its API.
 * Templates are expected at: transforms/{name}.jslt
 * No custom transformation logic or fallbacks are implemented.
 */
public final class JsonTransformer {
    private static final Logger log = LoggerFactory.getLogger(JsonTransformer.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final ConcurrentHashMap<String, Expression> expressionCache = new ConcurrentHashMap<>();

    /**
     * Apply the named transform to the provided JSON string and return the JSON result.
     * @param transformName logical name of the transform
     * @param inputJson input JSON to transform
     * @return transformed JSON string
     */
    public String transform(String transformName, String inputJson) {
        Objects.requireNonNull(transformName, "transformName");
        Objects.requireNonNull(inputJson, "inputJson");

        String templatePath = "transforms/" + transformName + ".jslt";
        if (!resourceExists(templatePath)) {
            log.error("Transform template not found: {}", templatePath);
            throw new IllegalArgumentException("Transform template not found: " + templatePath);
        }

        try {
            Expression expr = expressionCache.computeIfAbsent(templatePath, k -> {
                try {
                    String template = readResourceAsString(k);
                    return Parser.compileString(template);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to load or compile template: " + k, e);
                }
            });

            JsonNode inputNode = MAPPER.readTree(inputJson);
            JsonNode resultNode = expr.apply(inputNode);
            String result = MAPPER.writeValueAsString(resultNode);

            // Validate result JSON against optional schemas if present
            try {
                List<String> candidates = new ArrayList<>();
                // Schema aligned with transform resource path
                candidates.add("schemas/transforms/" + transformName + ".schema.json");
                // If the transform is namespaced (e.g., events/AssetUpserted), also try events schema by basename
                int slash = transformName.lastIndexOf('/') >= 0 ? transformName.lastIndexOf('/') : transformName.lastIndexOf('\\');
                if (slash >= 0 && slash < transformName.length() - 1) {
                    String base = transformName.substring(slash + 1);
                    candidates.add("schemas/events/" + base + ".schema.json");
                }

                // Validate against present schemas (if multiple candidates exist, validate against each).
                // If none are present, skip validation silently (best-effort validation).
                for (String schemaPath : candidates) {
                    try {
                        JsonSchemaValidator.validateIfPresent(result, schemaPath);
                    } catch (IllegalArgumentException iae) {
                        // If one candidate fails validation, propagate immediately
                        throw iae;
                    }
                }
            } catch (IllegalArgumentException iae) {
                log.error("JSLT transform '{}' produced JSON failing schema validation: {}", transformName, iae.getMessage());
                throw iae;
            } catch (Exception ignored) {
                // Best-effort validation: ignore unexpected errors in validator discovery
            }

            log.debug("Successfully applied JSLT transform '{}'", transformName);
            return result;
        } catch (Exception e) {
            log.error("JSLT transformation failed for '{}': {}", transformName, e.getMessage(), e);
            throw new IllegalArgumentException("Failed to transform JSON: " + e.getMessage(), e);
        }
    }

    private static boolean resourceExists(String path) {
        return JsonTransformer.class.getClassLoader().getResource(path) != null;
    }

    private static String readResourceAsString(String path) throws IOException {
        ClassLoader cl = JsonTransformer.class.getClassLoader();
        try (InputStream is = cl.getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("Transform template not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
