package com.db.assetstore.service;

import com.db.assetstore.AssetType;
import com.db.assetstore.model.Asset;
import com.db.assetstore.model.AttributeValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * More comprehensive tests for EventService JSON generation using JSLT.
 */
class EventServiceAdvancedTest {
    private static final ObjectMapper M = new ObjectMapper();

    @Test
    void generatesRichAssetUpsertedEvent_withVariousTypesAndNulls() throws Exception {
        Instant created = Instant.parse("2024-01-01T00:00:00Z");
        Asset asset = Asset.builder()
                .id("E-1")
                .type(AssetType.CRE)
                .createdAt(created)
                .attrs(List.of(
                        new AttributeValue<>("intAttr", 7, Integer.class),
                        new AttributeValue<>("longAttr", 1234567890123L, Long.class),
                        new AttributeValue<>("dblAttr", 3.14159, Double.class),
                        new AttributeValue<>("boolAttr", true, Boolean.class),
                        new AttributeValue<>("nullAttr", null, String.class),
                        new AttributeValue<>("tsAttr", created, Instant.class)
                ))
                .build();
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

        EventService svc = new EventService(new JsonTransformer());
        String eventJson = svc.generate("AssetUpserted", asset);
        JsonNode e = M.readTree(eventJson);

        assertEquals("AssetUpserted", e.get("eventName").asText());
        assertDoesNotThrow(() -> Instant.parse(e.get("occurredAt").asText()));
        assertEquals("E-1", e.get("id").asText());
        assertEquals("CRE", e.get("type").asText());
        assertEquals(created.toString(), e.get("createdAt").asText());
        assertEquals(42L, e.get("version").asLong());
        assertEquals("ACTIVE", e.get("status").asText());
        assertEquals("SUB", e.get("subtype").asText());
        assertEquals("2024-02-02T03:04:05Z", e.get("statusEffectiveTime").asText());
        assertEquals("2024-03-03T03:04:05Z", e.get("modifiedAt").asText());
        assertEquals("system", e.get("modifiedBy").asText());
        assertEquals("creator", e.get("createdBy").asText());
        assertTrue(e.get("softDelete").asBoolean());
        assertEquals(1000000.00, e.get("notionalAmount").asDouble(), 0.0001);
        assertEquals(2025, e.get("year").asInt());
        assertEquals("WH-1", e.get("wh").asText());
        assertEquals("CRM", e.get("sourceSystemName").asText());
        assertEquals("EXT-123", e.get("externalReference").asText());
        assertEquals("desc", e.get("description").asText());
        assertEquals("PLN", e.get("currency").asText());

        JsonNode attrs = e.get("attributes");
        assertTrue(attrs.isObject());
        assertEquals(7, attrs.get("intAttr").asInt());
        assertEquals(1234567890123L, attrs.get("longAttr").asLong());
        assertEquals(3.14159, attrs.get("dblAttr").asDouble(), 0.000001);
        assertTrue(attrs.get("boolAttr").asBoolean());
        assertTrue(attrs.get("nullAttr").isNull());
        // Non-scalar types are stringified by canonicalization
        assertEquals(created.toString(), attrs.get("tsAttr").asText());
    }
}
