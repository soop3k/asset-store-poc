package com.db.assetstore.service;

import com.db.assetstore.domain.model.asset.AssetType;
import com.db.assetstore.infra.json.AssetSerializer;
import com.db.assetstore.domain.model.asset.Asset;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.service.EventService;
import com.db.assetstore.domain.service.validation.JsonSchemaValidator;
import com.db.assetstore.infra.config.JsonMapperProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import com.db.assetstore.domain.service.transform.JsonTransformer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventServiceTest {
    private static final ObjectMapper mapper = new JsonMapperProvider().objectMapper();
    private static final AssetSerializer assetCanon = new AssetSerializer(mapper);
    private static final JsonSchemaValidator validator = new JsonSchemaValidator(mapper);
    private static final JsonTransformer transformer = new JsonTransformer(mapper, validator);

    @Test
    void generatesAssetEvent() throws Exception {
        Asset asset = Asset.builder()
                .id("A-1")
                .type(AssetType.CRE)
                .createdAt(Instant.parse("2024-01-01T00:00:00Z"))
                .build();
        asset.setAttributes(List.of(
                new AVString("city", "Gdańsk"),
                AVDecimal.of("rooms", 2)
        ));

        EventService svc = new EventService(transformer, assetCanon, mapper);
        String event = svc.generate("asset-cre", asset);
        JsonNode node = mapper.readTree(event);

        assertEquals("A-1", node.get("id").asText());
        assertEquals("CRE", node.get("type").asText());
        assertTrue(node.get("payload").isObject());
        assertEquals("Gdańsk", node.get("payload").get("city").asText());
        assertEquals(2, node.get("payload").get("rooms").asInt());
    }

    @Test
    void generatesRichAssetEvent() throws Exception {
        Instant created = Instant.parse("2024-01-01T00:00:00Z");
        Asset asset = Asset.builder()
                .id("E-1")
                .type(AssetType.CRE)
                .createdAt(created)
                .build();
        asset.setAttributes(List.of(
                AVDecimal.of("intAttr", 7),
                AVDecimal.of("longAttr", 1234567890123L),
                AVDecimal.of("dblAttr", 3.14159),
                AVBoolean.of("boolAttr", true),
                AVString.of("nullAttr", null),
                AVString.of("tsAttr", created.toString())
        ));
        asset.setVersion(42L);
        asset.setStatus("ACTIVE");
        asset.setSubtype("SUB");
        asset.setStatusEffectiveTime(Instant.parse("2024-02-02T03:04:05Z"));
        asset.setModifiedAt(Instant.parse("2024-03-03T03:04:05Z"));
        asset.setModifiedBy("system");
        asset.setCreatedBy("creator");
        asset.setSoftDelete(true);
        asset.setNotionalAmount(new BigDecimal("1000000.00"));
        asset.setYear(2025);
        asset.setWh("WH-1");
        asset.setSourceSystemName("CRM");
        asset.setExternalReference("EXT-123");
        asset.setDescription("desc");
        asset.setCurrency("PLN");

        EventService svc = new EventService(new JsonTransformer(mapper, validator), new AssetSerializer(mapper), mapper);
        String eventJson = svc.generate("asset-rich-cre", asset);
        JsonNode e = mapper.readTree(eventJson);

        assertEquals("E-1", e.get("id").asText());
        assertEquals("CRE", e.get("type").asText());
        assertEquals(1000000.00, e.get("notional_amount").asDouble(), 0.0001);
        assertEquals("PLN", e.get("currency").asText());

        JsonNode attrs = e.get("payload");
        assertTrue(attrs.isArray());
        assertEquals(7, attrs.get(0).get("intValue").asInt());
        assertEquals(1234567890123L, attrs.get(0).get("longValue").asLong());
        assertEquals(3.14159*2.0, attrs.get(0).get("doubleValue").asDouble(), 0.000001);
        assertFalse(attrs.has("boolAttr"));
        assertFalse(attrs.has("nullAttr"));
        assertFalse(attrs.has("tsAttr"));
    }

}
