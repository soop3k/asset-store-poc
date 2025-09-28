package com.db.assetstore.model;

import com.db.assetstore.domain.model.type.*;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AttributeValueTest {

    @Test
    void setDecimalAsValue() {
        var av = AVDecimal.of("rooms", new BigDecimal("2"));
        assertEquals("rooms", av.name());
        assertEquals(new BigDecimal("2"), av.value());
        assertEquals(AttributeType.DECIMAL, av.attributeType());
    }

    @Test
    void setDateAsValue() {
        var now = Instant.now();
        var av = AVDate.of("start", now);
        assertEquals("start", av.name());
        assertEquals(now, av.value());
        assertEquals(AttributeType.DATE, av.attributeType());
    }

    @Test
    void setStringAsValue() {
        var av = AVString.of("name", "House");
        assertEquals("name", av.name());
        assertEquals("House", av.value());
        assertEquals(AttributeType.STRING, av.attributeType());
    }

    @Test
    void setBooleanAsValue() {
        var av = AVBoolean.of("test", true);
        assertEquals("test", av.name());
        assertEquals(true, av.value());
        assertEquals(AttributeType.BOOLEAN, av.attributeType());
    }
}
