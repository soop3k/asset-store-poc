package com.db.assetstore.service;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AssetCanonicalizer;
import com.db.assetstore.domain.model.Asset;
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

/**
 * More comprehensive tests for EventService JSON generation using JSLT.
 */
class EventServiceAdvancedTest {
    private static final ObjectMapper M = new JsonMapperProvider().objectMapper();
    private static final JsonSchemaValidator validator = new JsonSchemaValidator(M);

    @Test
    void generatesRichAssetUpsertedEvent_withVariousTypesAndNulls() throws Exception {
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

        EventService svc = new EventService(new JsonTransformer(M, validator), new AssetCanonicalizer(M), M);
        String eventJson = svc.generate("asset-cre", asset);
        JsonNode e = M.readTree(eventJson);

        assertEquals("E-1", e.get("id").asText());
        assertEquals("CRE", e.get("type").asText());
        assertEquals(1000000.00, e.get("notional_amount").asDouble(), 0.0001);
        assertEquals("PLN", e.get("currency").asText());

        JsonNode attrs = e.get("payload");
        assertTrue(attrs.isObject());
        assertEquals(7, attrs.get("intAttr").asInt());
        assertEquals(1234567890123L, attrs.get("longAttr").asLong());
        assertEquals(3.14159, attrs.get("dblAttr").asDouble(), 0.000001);
        assertTrue(attrs.get("boolAttr").asBoolean());
        assertTrue(attrs.get("nullAttr").isNull());
        assertEquals(created.toString(), attrs.get("tsAttr").asText());
    }

}
