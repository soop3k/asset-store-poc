package com.db.assetstore.domain.service.transform;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schibsted.spt.data.jslt.Expression;
import com.schibsted.spt.data.jslt.Parser;
import com.db.assetstore.domain.service.validation.JsonSchemaValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@Component
public final class JsonTransformer {

    private final ObjectMapper objectMapper;
    private final JsonSchemaValidator validator;

    // TODO: Consider adding cache invalidation or reload support for template cache if templates change on disk
    private final ConcurrentHashMap<String, Expression> expressionCache = new ConcurrentHashMap<>();

    public String transform(String transformName, String inputJson) {
        Objects.requireNonNull(transformName, "transformName");
        Objects.requireNonNull(inputJson, "inputJson");

        // Pre-validate input JSON to provide clear error for malformed input
        validateInputJson(inputJson);

        String templatePath = "transforms/events/" + transformName + ".jslt";
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
                    throw new TemplateLoadingException("Failed to load or compile template: " + k, e);
                }
            });

            JsonNode inputNode = objectMapper.readTree(inputJson);
            JsonNode resultNode = expr.apply(inputNode);
            String result = objectMapper.writeValueAsString(resultNode);

            try {
                List.of(
                    "schemas/events/" + transformName + ".schema.json"
                ).forEach(schemaPath -> {
                    validator.validateOrThrow(result, schemaPath);
                });
            } catch (IllegalArgumentException iae) {
                log.error("Transform '{}' produced JSON failing schema validation: {}", transformName, iae.getMessage());
                throw iae;
            }

            log.info("Successfully applied transform '{}'", transformName);
            return result;
        } catch (Exception e) {
            log.error("Transformation failed for '{}': {}", transformName, e.getMessage(), e);
            throw new IllegalArgumentException("Failed to transform JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Pre-validates input JSON to provide clear error if input is malformed.
     */
    private void validateInputJson(String inputJson) {
        try {
            objectMapper.readTree(inputJson);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON input: " + e.getMessage(), e);
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
