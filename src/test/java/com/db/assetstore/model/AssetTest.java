package com.db.assetstore.model;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AssetTest {

    @Test
    void setAttributeAndSetAttributesWork() {
        Map<String, List<AttributeValue<?>>> init = Map.of(
                "city", List.of(new AVString("city", "Warsaw"))
        );
        Asset a = Asset.builder()
                .id("id")
                .type(AssetType.CRE)
                .createdAt(Instant.now())
                .attributes(AttributesCollection.fromMap(init))
                .build();
        assertEquals("Warsaw", a.getAttributesByName().get("city").get(0).value());

        a.setAttribute(AVString.of("city", "Krakow"));
        List<AttributeValue<?>> cities = a.getAttributesByName().get("city");
        assertEquals(2, cities.size());
        assertEquals("Krakow", cities.get(1).value());

        a.setAttribute(AVDecimal.of("rooms", new BigDecimal("3")));
        assertEquals(new BigDecimal("3"), a.getAttributesByName().get("rooms").get(0).value());

        a.setAttributes(List.of(new AVBoolean("active", true)));
        Map<String, List<AttributeValue<?>>> attrs = a.getAttributesByName();
        assertTrue(attrs.containsKey("active"));
        assertEquals(Boolean.TRUE, attrs.get("active").get(0).value());
        assertFalse(attrs.containsKey("rooms"));
        assertFalse(attrs.containsKey("city"));
    }

    @Test
    void attributesCollectionAccessorsWork() {
        Asset a2 = Asset.builder()
                .id("id")
                .type(AssetType.CRE)
                .createdAt(Instant.now())
                .attributes(AttributesCollection.empty())
                .build();
        a2.setAttributes(List.of(
                AVString.of("city", "Gdansk"),
                AVDecimal.of("rooms", 2),
                AVBoolean.of("active", true)
        ));

        AttributesCollection attrSet = AttributesCollection.fromFlat(a2.getAttributesFlat());
        assertEquals("Gdansk", attrSet.get("city").orElseThrow().value());
        assertEquals(new java.math.BigDecimal("2"), attrSet.get("rooms").orElseThrow().value());
        assertEquals(Boolean.TRUE, attrSet.get("active").orElseThrow().value());

        assertTrue(attrSet.get("price").isEmpty());
    }
}
