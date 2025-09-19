package com.db.assetstore.service;

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
                "\"attributes\":{\"city\":\"Warsaw\",\"area\":100.5,\"rooms\":3,\"active\":true}" +
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
    void generatesIdWhenMissingOrBlank() {
        String jsonMissing = "{\"type\":\"CRE\",\"attributes\":{}}";
        Asset a1 = factory.fromJson(jsonMissing);
        assertNotNull(a1.getId());
        assertFalse(a1.getId().isBlank());

        String jsonBlank = "{\"type\":\"CRE\",\"id\":\"\",\"attributes\":{}}";
        Asset a2 = factory.fromJson(jsonBlank);
        assertNotNull(a2.getId());
        assertFalse(a2.getId().isBlank());
    }

    @Test
    void handlesNullAndNonScalarAttributesAsString() {
        String json = "{" +
                "\"type\":\"CRE\"," +
                "\"attributes\":{\"note\":null,\"obj\":{\"a\":1},\"arr\":[1,2,3]}" +
                "}";
        Asset a = factory.fromJson(json);
        Map<String, AttributeValue<?>> attrs = a.attributes();
        assertNull(attrs.get("note").value());
        assertNotNull(attrs.get("obj").value());
        assertTrue(attrs.get("obj").value() instanceof String);
        assertTrue(attrs.get("arr").value() instanceof String);
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
