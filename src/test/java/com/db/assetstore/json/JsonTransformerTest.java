package com.db.assetstore.json;

import com.db.assetstore.domain.service.validation.JsonSchemaValidator;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.db.assetstore.domain.service.transform.JsonTransformer;

import static org.junit.jupiter.api.Assertions.*;

class JsonTransformerTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final JsonSchemaValidator validator = new JsonSchemaValidator(mapper);

    @Test
    void transformsUsingJsltAndValidatesAgainstSchema() throws Exception {
        JsonTransformer tr = new JsonTransformer(mapper, validator);
        String input = """
            { "asset": {
                  "id": "id-9",
                  "type": "CRE",
                  "attributes": {
                    "city": "Warsaw",
                    "rooms": 3
                  }
                }
            }
            """;
        String out = tr.transform("asset-cre", input);
        JsonNode node = mapper.readTree(out);
        assertEquals("id-9", node.get("id").asText());
        assertEquals("CRE", node.get("type").asText());
        assertTrue(node.get("payload").isObject());
        assertEquals(3, node.get("payload").get("rooms").asInt());
    }

    @Test
    void missingSchemaDoesNotFailWhenNotProvided() throws Exception {
        JsonTransformer tr = new JsonTransformer(mapper, validator);
        String input = "{\"x\":1}";
        assertThrows(IllegalArgumentException.class, () -> tr.transform("non-existing", input));
    }
}
