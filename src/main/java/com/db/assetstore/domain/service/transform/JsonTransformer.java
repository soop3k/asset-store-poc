package com.db.assetstore.domain.service.transform;

import com.db.assetstore.domain.exception.JsonTransformException;
import com.db.assetstore.domain.exception.TransformSchemaValidationException;
import com.db.assetstore.domain.exception.TransformTemplateNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schibsted.spt.data.jslt.Expression;
import com.schibsted.spt.data.jslt.Parser;
import com.db.assetstore.domain.service.validation.JsonSchemaValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@Component
public final class JsonTransformer {

    private final ObjectMapper objectMapper;
    private final JsonSchemaValidator validator;

    private final ConcurrentHashMap<String, Expression> expressionCache = new ConcurrentHashMap<>();

    public String transform(String transformName, String inputJson) throws JsonTransformException {
        Objects.requireNonNull(transformName, "transformName");
        Objects.requireNonNull(inputJson, "inputJson");

        String templatePath = "transforms/events/" + transformName + ".jslt";
        Expression expr = loadExpression(templatePath);

        try {
            JsonNode inputNode = objectMapper.readTree(inputJson);
            JsonNode resultNode = expr.apply(inputNode);
            String result = objectMapper.writeValueAsString(resultNode);

            try {
                String schemaPath = "schemas/events/" + transformName + ".schema.json";
                validator.validateOrThrow(result, schemaPath);
            } catch (IllegalArgumentException iae) {
                log.error("Transform '{}' produced JSON failing schema validation: {}", transformName, iae.getMessage());
                throw new TransformSchemaValidationException(
                        "Transform '" + transformName + "' produced invalid payload: " + iae.getMessage());
            }

            log.info("Successfully applied transform '{}'", transformName);
            return result;
        } catch (JsonTransformException e) {
            throw e;
        } catch (Exception e) {
            log.error("Transformation failed for '{}': {}", transformName, e.getMessage(), e);
            throw new JsonTransformException("Failed to transform JSON for transform '" + transformName + "'", e);
        }
    }

    private Expression loadExpression(String templatePath) throws JsonTransformException {
        Expression cached = expressionCache.get(templatePath);
        if (cached != null) {
            return cached;
        }

        Expression compiled = compileTemplate(templatePath);
        Expression existing = expressionCache.putIfAbsent(templatePath, compiled);
        return existing != null ? existing : compiled;
    }

    private Expression compileTemplate(String templatePath) throws JsonTransformException {
        try {
            String template = readResourceAsString(templatePath);
            return Parser.compileString(template);
        } catch (TransformTemplateNotFoundException e) {
            log.error("Transform template not found: {}", templatePath);
            throw e;
        } catch (JsonTransformException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to load or compile template {}: {}", templatePath, e.getMessage());
            throw new JsonTransformException("Failed to load or compile template: " + templatePath, e);
        }
    }

    private String readResourceAsString(String path) throws JsonTransformException {
        ClassLoader cl = JsonTransformer.class.getClassLoader();
        try (InputStream is = cl.getResourceAsStream(path)) {
            if (is == null) {
                throw new TransformTemplateNotFoundException(path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new JsonTransformException("Failed to read template: " + path, e);
        }
    }
}
