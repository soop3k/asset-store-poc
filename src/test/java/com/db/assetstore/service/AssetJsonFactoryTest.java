package com.db.assetstore.service;

import com.db.assetstore.json.AssetJsonFactory;

import com.db.assetstore.AssetType;
import com.db.assetstore.model.Asset;
import com.db.assetstore.model.AttributeValue;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AssetJsonFactoryTest {

    private final AssetJsonFactory factory = new AssetJsonFactory();

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
        Map<String, AttributeValue<?>> attrs = a.attributes();
        assertEquals("Warsaw", attrs.get("city").value());
        assertEquals(100.5d, (Double) attrs.get("area").value(), 0.0001);
        assertEquals(3L, attrs.get("rooms").value());
        assertEquals(Boolean.TRUE, attrs.get("active").value());
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
    void handlesUnknownAndNonScalarAttributesIgnored_whenNotDefined() {
        String json = "{" +
                "\"type\":\"CRE\"," +
                "\"id\":\"id-456\"," +
                "\"city\":\"Warsaw\",\"note\":null,\"obj\":{\"a\":1},\"arr\":[1,2,3]" +
                "}";
        Asset a = factory.fromJson(json);
        Map<String, AttributeValue<?>> attrs = a.attributes();
        assertEquals(1, attrs.size());
        assertEquals("Warsaw", attrs.get("city").value());
        assertFalse(attrs.containsKey("note"));
        assertFalse(attrs.containsKey("obj"));
        assertFalse(attrs.containsKey("arr"));
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
