package com.db.assetstore.service.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;
import java.net.URL;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

/**
 * JSON Schema validation facade using networknt json-schema-validator directly.
 * Validates exactly per schema; any validation error results in IllegalArgumentException.
 */
public final class JsonSchemaValidator {
    private static final Logger log = LoggerFactory.getLogger(JsonSchemaValidator.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonSchemaValidator() {}

    public static void validateOrThrow(String jsonPayload, String schemaClasspathResource) {
        try {
            if (schemaClasspathResource == null) {
                return; // no schema -> skip
            }
            InputStream is = JsonSchemaValidator.class.getClassLoader().getResourceAsStream(schemaClasspathResource);
            if (is == null) {
                log.error("Schema not found: {}", schemaClasspathResource);
                throw new IllegalArgumentException("Schema not found: " + schemaClasspathResource);
            }
            JsonNode schema = MAPPER.readTree(is);
            JsonNode payload = MAPPER.readTree(jsonPayload);
            log.debug("Validating JSON against schema: {}", schemaClasspathResource);

            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            JsonSchema compiled = factory.getSchema(schema);
            Set<ValidationMessage> result = compiled.validate(payload);

            if (result != null && !result.isEmpty()) {
                List<String> messages = new ArrayList<>();
                for (ValidationMessage vm : result) {
                    messages.add(vm.getMessage());
                }
                String joined = String.join("; ", messages);
                throw new IllegalArgumentException("JSON schema validation failed: " + joined);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to validate JSON against schema {}: {}", schemaClasspathResource, e.getMessage(), e);
            throw new IllegalArgumentException("Failed to validate JSON against schema: " + e.getMessage(), e);
        }
    }

    public static void validateIfPresent(String jsonPayload, String schemaClasspathResource) {
        try {
            if (schemaClasspathResource == null) {
                return;
            }
            URL url = JsonSchemaValidator.class.getClassLoader().getResource(schemaClasspathResource);
            if (url == null) {
                return; // nothing to validate against
            }
        } catch (Exception ignored) {
            return;
        }
        validateOrThrow(jsonPayload, schemaClasspathResource);
    }
}