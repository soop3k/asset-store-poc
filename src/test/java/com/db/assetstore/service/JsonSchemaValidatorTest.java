package com.db.assetstore.service;

import org.junit.jupiter.api.Test;
import com.db.assetstore.service.validation.JsonSchemaValidator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonSchemaValidatorTest {

    @Test
    void ignoresUnknownProperties_whenSchemaDisallowsThem() {
        String payload = "{\"foo\":\"bar\",\"extra\":123}";
        assertDoesNotThrow(() -> JsonSchemaValidator.validateOrThrow(payload, "schemas/strict.schema.json"));
    }

    @Test
    void stillFailsOnRealSchemaErrors_likeTypeMismatch() {
        String payload = "{\"foo\":123}"; // foo should be string per schema
        assertThrows(IllegalArgumentException.class, () -> JsonSchemaValidator.validateOrThrow(payload, "schemas/strict.schema.json"));
    }
}
