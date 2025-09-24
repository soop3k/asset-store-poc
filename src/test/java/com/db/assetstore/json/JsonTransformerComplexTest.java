package com.db.assetstore.json;

import com.db.assetstore.domain.service.validation.JsonSchemaValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import com.db.assetstore.domain.service.transform.JsonTransformer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * More advanced tests for JSON transformation using JSLT.
 * These tests focus on JSON structure fidelity (arrays, objects, nulls, unicode) and basic concurrency.
 */
class JsonTransformerComplexTest {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final JsonSchemaValidator validator = new JsonSchemaValidator(mapper);

    @Test
    void preservesNestedArraysAndObjects_inPayload() throws Exception {
        JsonTransformer tr = new JsonTransformer(mapper, validator);
        String input = """
                {"asset": {
                "id":"id-77",
                "type":"CRE",
                "attributes":{
                  "address":{"city":"Łódź","zip":"90-001"},
                  "rooms":[1,2,3],
                  "features":[{"name":"balcony"},{"name":"garage"}],
                  "note":null
                }}}""";

        String out = tr.transform("asset-cre", input);
        JsonNode node = mapper.readTree(out);

        assertEquals("id-77", node.get("id").asText());
        assertEquals("CRE", node.get("type").asText());

        JsonNode payload = node.get("payload");
        assertTrue(payload.isObject());
        assertTrue(payload.get("address").isObject());
        assertEquals("Łódź", payload.get("address").get("city").asText());
        assertEquals("90-001", payload.get("address").get("zip").asText());
        assertTrue(payload.get("rooms").isArray());
        assertEquals(3, payload.get("rooms").size());
        assertEquals(2, payload.get("rooms").get(1).asInt());
        assertTrue(payload.get("features").isArray());
        assertEquals("balcony", payload.get("features").get(0).get("name").asText());
        assertTrue(payload.get("note").isNull());
    }

    @Test
    void missingFieldsBecomeNullInOutput() throws Exception {
        JsonTransformer tr = new JsonTransformer(mapper, validator);
        String input = """
                { "asset": {
                    "id": "x-1",
                    "attributes": {}
                }}""";

        String out = tr.transform("asset-cre", input);
        JsonNode node = mapper.readTree(out);
        assertEquals("x-1", node.get("id").asText());
        assertFalse(node.has("kind"));
    }

    @Test
    void supportsUnicodeAndEscapedCharacters() throws Exception {
        JsonTransformer tr = new JsonTransformer(mapper, validator);
        String emoji = "🏡";
        String quoteText = "\"quoted\"";

        var root = mapper.createObjectNode();
        var asset = mapper.createObjectNode();
        root.set("asset", asset);
        asset.put("id", "home-😊");
        asset.put("type", "CRE");
        var attrs = mapper.createObjectNode();
        attrs.put("title", emoji + " " + quoteText);
        asset.set("attributes", attrs);
        String input = mapper.writeValueAsString(root);

        String out = tr.transform("asset-cre", input);
        JsonNode node = mapper.readTree(out);
        assertEquals("home-😊", node.get("id").asText());
        assertEquals("CRE", node.get("type").asText());
        assertEquals(emoji + " " + quoteText, node.get("payload").get("title").asText());

        byte[] bytes = out.getBytes(StandardCharsets.UTF_8);
        String roundTrip = new String(bytes, StandardCharsets.UTF_8);
        assertEquals(out, roundTrip);
    }

}
