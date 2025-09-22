package com.db.assetstore.model;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.AttributeValue;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AssetTest {

    @Test
    void setAttributeAndSetAttributesWork() {
        Asset a = new Asset("id", AssetType.CRE, Instant.now(), List.of(new AttributeValue<>("city", "Warsaw", String.class)));
        assertEquals("Warsaw", a.attributes().get("city").value());

        a.setAttribute(new AttributeValue<>("city", "Krakow", String.class));
        assertEquals("Krakow", a.attributes().get("city").value());

        a.setAttribute("rooms", 3L, Long.class);
        assertEquals(3L, a.attributes().get("rooms").value());

        a.setAttributes(List.of(new AttributeValue<>("active", true, Boolean.class)));
        Map<String, AttributeValue<?>> attrs = a.attributes();
        assertEquals(Boolean.TRUE, attrs.get("active").value());
        assertEquals(3L, attrs.get("rooms").value());
        assertEquals("Krakow", attrs.get("city").value());
    }

    @Test
    void getAttributeGenericWorksAndIsTypeSafe() {
        Asset a = new Asset("id", AssetType.CRE, Instant.now(), List.of(
                new AttributeValue<>("city", "Gdansk", String.class),
                new AttributeValue<>("rooms", 2L, Long.class)
        ));

        Optional<String> city = a.getAttribute("city", String.class);
        assertTrue(city.isPresent());
        assertEquals("Gdansk", city.get());

        Optional<Long> rooms = a.getAttribute("rooms", Long.class);
        assertEquals(2L, rooms.orElseThrow());

        // Missing attribute
        assertTrue(a.getAttribute("price", Long.class).isEmpty());
        // Type mismatch should throw
        assertThrows(ClassCastException.class, () -> a.getAttribute("city", Long.class));
    }

    @Test
    void getAttributeRequiresExplicitTypeAndChecksInside() {
        Asset a = new Asset("id", AssetType.CRE, Instant.now(), List.of(
                new AttributeValue<>("city", "Gdansk", String.class),
                new AttributeValue<>("rooms", 2L, Long.class),
                new AttributeValue<>("active", true, Boolean.class)
        ));

        Optional<String> city = a.getAttribute("city", String.class);
        assertTrue(city.isPresent());
        assertEquals("Gdansk", city.get());

        Optional<Long> roomsAsLong = a.getAttribute("rooms", Long.class);
        assertTrue(roomsAsLong.isPresent());
        assertEquals(2L, roomsAsLong.get());

        Optional<Boolean> active = a.getAttribute("active", Boolean.class);
        assertEquals(Boolean.TRUE, active.orElse(null));

        // Missing attribute still empty
        assertTrue(a.getAttribute("price", Long.class).isEmpty());

        // Mismatch still throws from inside typed getAttribute
        assertThrows(ClassCastException.class, () -> a.getAttribute("city", Long.class));
    }
}
