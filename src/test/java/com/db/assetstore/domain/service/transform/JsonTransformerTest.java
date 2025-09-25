package com.db.assetstore.domain.service.transform;

import com.db.assetstore.domain.exception.JsonTransformException;
import com.db.assetstore.domain.exception.TransformSchemaValidationException;
import com.db.assetstore.domain.exception.TransformTemplateNotFoundException;
import com.db.assetstore.domain.service.validation.JsonSchemaValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class JsonTransformerTest {

    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void transform_whenTemplateMissing_throwsNotFoundException() {
        JsonSchemaValidator validator = new JsonSchemaValidator(objectMapper);
        JsonTransformer transformer = new JsonTransformer(objectMapper, validator);

        assertThatThrownBy(() -> transformer.transform("missing-template", "{}"))
                .isInstanceOf(TransformTemplateNotFoundException.class)
                .hasMessageContaining("missing-template");
    }

    @Test
    void transform_whenValidatorFails_wrapsIntoSchemaValidationException() {
        JsonSchemaValidator validator = mock(JsonSchemaValidator.class);
        JsonTransformer transformer = new JsonTransformer(objectMapper, validator);

        doThrow(new IllegalArgumentException("schema broken"))
                .when(validator).validateOrThrow(any(String.class), any(String.class));

        String input = "{\"asset\":{\"id\":\"1\",\"type\":\"CRE\"}}";

        assertThatThrownBy(() -> transformer.transform("asset-cre", input))
                .isInstanceOf(TransformSchemaValidationException.class)
                .hasMessageContaining("asset-cre")
                .hasMessageContaining("schema broken");
    }

    @Test
    void transform_whenInputJsonInvalid_wrapsAsJsonTransformException() {
        JsonSchemaValidator validator = new JsonSchemaValidator(objectMapper);
        JsonTransformer transformer = new JsonTransformer(objectMapper, validator);

        assertThatThrownBy(() -> transformer.transform("asset-cre", "not-json"))
                .isInstanceOf(JsonTransformException.class)
                .hasMessageContaining("asset-cre");
    }
}
