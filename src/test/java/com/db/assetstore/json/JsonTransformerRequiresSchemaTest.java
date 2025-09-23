package com.db.assetstore.json;

import org.junit.jupiter.api.Test;
import com.db.assetstore.domain.service.transform.JsonTransformer;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

class JsonTransformerRequiresSchemaTest {

    private static final ObjectMapper M = new ObjectMapper();

    @Test
    void transformWithoutOutputSchema_shouldSucceed() {
        JsonTransformer tr = new JsonTransformer(M);
        String input = "{}";
        assertDoesNotThrow(() -> tr.transform("no-schema-pass", input));
    }
}
