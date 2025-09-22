package com.db.assetstore.service;

import org.junit.jupiter.api.Test;
import com.db.assetstore.domain.service.transform.JsonTransformer;

import static org.junit.jupiter.api.Assertions.*;

class JsonTransformerRequiresSchemaTest {

    @Test
    void transformWithoutOutputSchema_shouldSucceed() {
        JsonTransformer tr = new JsonTransformer();
        String input = "{}";
        assertDoesNotThrow(() -> tr.transform("no-schema-pass", input));
    }
}
