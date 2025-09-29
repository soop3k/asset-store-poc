package com.db.assetstore.json;

import com.db.assetstore.infra.config.JsonMapperProvider;
import org.junit.jupiter.api.Test;
import com.db.assetstore.domain.service.validation.JsonSchemaValidator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonSchemaValidatorTest {

    @Test
    void ignoresUnknownProperties() {
        String payload = """ 
           {
             "foo":"bar",
             "extra":123
           }""";
        assertThrows(IllegalArgumentException.class, () -> createSchemaValidator().validateOrThrow(
                payload, "schemas/strict.schema.json"));
    }

    @Test
    void failsOnSchemaErrors() {
        String payload = """
             {
               "foo": 123
             }""";
        assertThrows(IllegalArgumentException.class, () -> createSchemaValidator().validateOrThrow(
                payload, "schemas/strict.schema.json"));
    }

    private JsonSchemaValidator createSchemaValidator() {
        return new JsonSchemaValidator(new JsonMapperProvider().objectMapper());
    }
}
