package com.db.assetstore.json;

import com.db.assetstore.infra.config.JsonMapperProvider;
import org.junit.jupiter.api.Test;
import com.db.assetstore.domain.service.validation.JsonSchemaValidator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonSchemaValidatorTest {

    @Test
    void ignoresUnknownProperties_whenSchemaDisallowsThem() {
        String payload = "{\"foo\":\"bar\",\"extra\":123}";
        var mapper = new JsonMapperProvider().objectMapper();
        var validator = new JsonSchemaValidator(mapper);
        assertThrows(IllegalArgumentException.class, () -> validator.validateOrThrow(
                payload, "schemas/strict.schema.json"));
    }

    @Test
    void stillFailsOnRealSchemaErrors_likeTypeMismatch() {
        String payload = "{\"foo\":123}";
        var mapper = new JsonMapperProvider().objectMapper();
        var validator = new JsonSchemaValidator(mapper);
        assertThrows(IllegalArgumentException.class, () -> validator.validateOrThrow(
                payload, "schemas/strict.schema.json"));
    }
}
