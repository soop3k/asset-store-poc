package com.db.assetstore.json;

import com.db.assetstore.domain.json.AssetJsonFactory;
import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.schema.TypeSchemaRegistry;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.db.assetstore.infra.config.JsonMapperProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AssetJsonFactoryTest {

    private static final ObjectMapper M = new JsonMapperProvider().objectMapper();
    private final TypeSchemaRegistry typeSchema = new TypeSchemaRegistry();
    private final AttributeDefinitionRegistry registry = new AttributeDefinitionRegistry(M, typeSchema);
    private final AttributeJsonReader reader = new AttributeJsonReader(M, registry);
    private final AssetJsonFactory factory = new AssetJsonFactory(M, reader, registry);

    @Test
    void parsesValidJsonWithExplicitIdAndAttributes() {
        String json = "{" +
                "\"type\":\"CRE\"," +
                "\"id\":\"id-123\"," +
                "\"city\":\"Warsaw\",\"area\":100.5,\"rooms\":3,\"active\":true" +
                "}";
        Asset a = factory.fromJson(json);
        assertEquals("id-123", a.getId());
        assertEquals(AssetType.CRE, a.getType());
        Map<String, List<AttributeValue<?>>> attrs = a.getAttributesByName();
        assertEquals("Warsaw", attrs.get("city").get(0).value());
        assertEquals(new BigDecimal("100.5"), attrs.get("area").get(0).value());
        assertEquals(new BigDecimal("3"), attrs.get("rooms").get(0).value());
        assertEquals(Boolean.TRUE, attrs.get("active").get(0).value());
    }

    @Test
    void throwsOnMissingOrBlankId() {
        String jsonMissing = "{\"type\":\"CRE\",\"city\":\"Warsaw\"}";
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> factory.fromJson(jsonMissing));
        assertTrue(ex1.getMessage().toLowerCase().contains("missing 'id'"));

        String jsonBlank = "{\"type\":\"CRE\",\"id\":\"\",\"city\":\"Warsaw\"}";
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> factory.fromJson(jsonBlank));
        assertTrue(ex2.getMessage().toLowerCase().contains("missing 'id'"));
    }

    @Test
    void throwsOnMissingType() {
        String json = "{\"id\":\"x\"}";
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> factory.fromJson(json));
        assertTrue(ex.getMessage().toLowerCase().contains("missing 'type'"));
    }

    @Test
    void throwsOnUnknownType() {
        String json = "{\"type\":\"UNKNOWN\"}";
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> factory.fromJson(json));
        assertTrue(ex.getMessage().toLowerCase().contains("unknown asset type"));
    }
}
