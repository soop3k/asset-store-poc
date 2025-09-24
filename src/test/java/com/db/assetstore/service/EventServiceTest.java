package com.db.assetstore.service;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AssetCanonicalizer;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.service.EventService;
import com.db.assetstore.domain.service.validation.JsonSchemaValidator;
import com.db.assetstore.infra.config.JsonMapperProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import com.db.assetstore.domain.service.transform.JsonTransformer;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventServiceTest {
    private static final ObjectMapper M = new JsonMapperProvider().objectMapper();
    private static final AssetCanonicalizer assetCanon = new AssetCanonicalizer(M);
    private static final JsonSchemaValidator validator = new JsonSchemaValidator(M);

    @Test
    void generatesAssetUpsertedEvent_usingJslt_and_validatesIfSchemaPresent() throws Exception {
        Asset asset = Asset.builder()
                .id("A-1")
                .type(AssetType.CRE)
                .createdAt(Instant.parse("2024-01-01T00:00:00Z"))
                .build();
        asset.setAttributes(List.of(
                new AVString("city", "Gdańsk"),
                AVDecimal.of("rooms", 2)
        ));

        EventService svc = new EventService(new JsonTransformer(M, validator), assetCanon, M);
        String event = svc.generate("asset-cre", asset);
        JsonNode node = M.readTree(event);

        assertEquals("A-1", node.get("id").asText());
        assertEquals("CRE", node.get("type").asText());
        assertTrue(node.get("payload").isObject());
        assertEquals("Gdańsk", node.get("payload").get("city").asText());
        assertEquals(2, node.get("payload").get("rooms").asInt());
    }

}
