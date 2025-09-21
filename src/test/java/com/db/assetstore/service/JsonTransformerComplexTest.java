package com.db.assetstore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
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
    private static final ObjectMapper M = new ObjectMapper();

    @Test
    void preservesNestedArraysAndObjects_inPayload() throws Exception {
        JsonTransformer tr = new JsonTransformer();
        String input = "{" +
                "\"id\":\"id-77\"," +
                "\"type\":\"CRE\"," +
                "\"attributes\":{" +
                "  \"address\":{\"city\":\"Łódź\",\"zip\":\"90-001\"}," +
                "  \"rooms\":[1,2,3]," +
                "  \"features\":[{\"name\":\"balcony\"},{\"name\":\"garage\"}]," +
                "  \"note\":null" +
                "}}";

        String out = tr.transform("asset-to-external", input);
        JsonNode node = M.readTree(out);

        assertEquals("id-77", node.get("assetId").asText());
        assertEquals("CRE", node.get("kind").asText());

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
        JsonTransformer tr = new JsonTransformer();
        String input = "{" +
                "\"id\":\"x-1\"," +
                // intentionally no type
                "\"attributes\":{}}";

        String out = tr.transform("asset-to-external", input);
        JsonNode node = M.readTree(out);
        assertEquals("x-1", node.get("assetId").asText());
        // JSLT omits fields whose value evaluates to null, so 'kind' should be absent
        assertFalse(node.has("kind"));
    }

    @Test
    void supportsUnicodeAndEscapedCharacters() throws Exception {
        JsonTransformer tr = new JsonTransformer();
        String emoji = "🏡";
        String quoteText = "\"quoted\""; // literal quotes inside the string

        // Build input JSON programmatically to avoid escaping issues
        var root = M.createObjectNode();
        root.put("id", "home-😊");
        root.put("type", "CRE");
        var attrs = M.createObjectNode();
        attrs.put("title", emoji + " " + quoteText);
        root.set("attributes", attrs);
        String input = M.writeValueAsString(root);

        String out = tr.transform("asset-to-external", input);
        JsonNode node = M.readTree(out);
        assertEquals("home-😊", node.get("assetId").asText());
        assertEquals("CRE", node.get("kind").asText());
        assertEquals(emoji + " " + quoteText, node.get("payload").get("title").asText());

        // Ensure JSON is valid UTF-8 round-trip
        byte[] bytes = out.getBytes(StandardCharsets.UTF_8);
        String roundTrip = new String(bytes, StandardCharsets.UTF_8);
        assertEquals(out, roundTrip);
    }

    @Test
    void concurrentTransformations_areThreadSafeAndConsistent() throws Exception {
        JsonTransformer tr = new JsonTransformer();
        String input = "{" +
                "\"id\":\"bulk-99\"," +
                "\"type\":\"CRE\"," +
                "\"attributes\":{\"rooms\":5}}";
        String expected = tr.transform("asset-to-external", input);

        int threads = 8;
        int perThread = 25;
        var pool = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads * perThread);
        List<Throwable> failures = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                for (int i = 0; i < perThread; i++) {
                    try {
                        String out = tr.transform("asset-to-external", input);
                        assertEquals(expected, out);
                    } catch (Throwable th) {
                        synchronized (failures) { failures.add(th); }
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Timed out waiting for transformations");
        pool.shutdownNow();
        if (!failures.isEmpty()) {
            failures.forEach(Throwable::printStackTrace);
            fail("Concurrent transformations had failures: " + failures.size());
        }
    }
}
