package com.db.assetstore.service;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.AttributeValue;
import com.db.assetstore.domain.service.EventService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import com.db.assetstore.domain.service.transform.JsonTransformer;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventServiceTest {
    private static final ObjectMapper M = new ObjectMapper();

    @Test
    void generatesAssetUpsertedEvent_usingJslt_and_validatesIfSchemaPresent() throws Exception {
        Asset asset = Asset.builder()
                .id("A-1")
                .type(AssetType.CRE)
                .createdAt(Instant.parse("2024-01-01T00:00:00Z"))
                .attrs(List.of(
                        new AttributeValue<>("city", "Gdansk", String.class),
                        new AttributeValue<>("rooms", 2, Integer.class)
                ))
                .build();

        EventService svc = new EventService(new JsonTransformer());
        String event = svc.generate("AssetUpserted", asset);
        JsonNode node = M.readTree(event);

        assertEquals("AssetUpserted", node.get("eventName").asText());
        assertEquals("A-1", node.get("id").asText());
        assertEquals("CRE", node.get("type").asText());
        assertTrue(node.get("attributes").isObject());
        assertEquals("Gdansk", node.get("attributes").get("city").asText());
        assertEquals(2, node.get("attributes").get("rooms").asInt());
    }
}
