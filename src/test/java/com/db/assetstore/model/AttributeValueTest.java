package com.db.assetstore.model;

import com.db.assetstore.domain.model.type.AttributeType;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.type.AVDecimal;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AttributeValueTest {

    @Test
    void gettersReturnValues() {
        AttributeValue<BigDecimal> av = AVDecimal.of("rooms", new BigDecimal("2"));
        assertEquals("rooms", av.name());
        assertEquals(new BigDecimal("2"), av.value());
        assertEquals(AttributeType.DECIMAL, av.attributeType());
    }
}
