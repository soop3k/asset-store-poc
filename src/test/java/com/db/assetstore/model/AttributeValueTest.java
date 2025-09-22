package com.db.assetstore.model;

import com.db.assetstore.domain.model.AttributeValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttributeValueTest {

    @Test
    void gettersReturnValues() {
        AttributeValue<Long> av = new AttributeValue<>("rooms", 2L, Long.class);
        assertEquals("rooms", av.name());
        assertEquals(2L, av.value());
        assertEquals(Long.class, av.type());
    }
}
