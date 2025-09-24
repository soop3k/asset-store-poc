package com.db.assetstore.domain.service.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public final class JsonSchemaValidator {

    private final ObjectMapper mapper;
    private final JsonSchemaFactory factory =
            JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);

    public void validateOrThrow(JsonNode payload, JsonSchema compiled, String schemaNameForLogs) {
        log.info("Validating JSON against schema: {}", schemaNameForLogs);
        List<String> errors = validate(payload, compiled);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(
                    "JSON schema validation failed (" + schemaNameForLogs + "): " + String.join("; ", errors));
        }
    }

    public List<String> validate(JsonNode payload, JsonSchema compiled) {
        Set<ValidationMessage> v = compiled.validate(payload);
        if (v == null || v.isEmpty()) return Collections.emptyList();
        return v.stream().map(ValidationMessage::getMessage).sorted().toList();
    }

    public void validateOrThrow(String json, String schemaRes) {
        if (isBlank(schemaRes)) return;
        log.info("Validating JSON against schema: {}", schemaRes);
        JsonNode payload = parse(json);
        JsonSchema compiled = compileSchema(schemaRes);
        if (compiled == null) {
            return;
        }

        List<String> errors = validate(payload, compiled);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(
                    "JSON schema validation failed (" + schemaRes + "): " + String.join("; ", errors));
        }
    }

    private JsonSchema compileSchema(String schemaRes) {
        try (InputStream is = JsonSchemaValidator.class.getClassLoader().getResourceAsStream(schemaRes)) {
            if (is == null) {
                throw new IllegalArgumentException("Schema not found: " + schemaRes);
            }

            JsonNode node = mapper.readTree(is);

            return factory.getSchema(node,
                    SchemaValidatorsConfig.builder().failFast(true).typeLoose(true).build()
            );

        } catch (Exception e) {
            log.error("Failed to load/compile schema [{}]: {}", schemaRes, e.getMessage(), e);
            throw new IllegalArgumentException("Failed to load/compile schema [" + schemaRes + "]: " + e.getMessage(), e);
        }
    }

    private JsonNode parse(String json) {
        if (isBlank(json)) throw new IllegalArgumentException("Payload JSON is null/blank");
        try {
            return mapper.readTree(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Invalid JSON payload: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Invalid JSON payload: " + e.getMessage(), e);
        }
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
