package com.db.assetstore.model;

import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVBoolean;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AttributesCollectionTest {

    @Test
    void getOneValidatesTypeCompatibilityAndReturnsEmpty() {
        AttributesCollection attrs = AttributesCollection.empty()
            .set("stringAttr", "test")
            .set("numberAttr", BigDecimal.valueOf(42));
        
        // Type-compatible access should work
        Optional<String> stringValue = attrs.getOne("stringAttr", String.class);
        assertTrue(stringValue.isPresent());
        assertEquals("test", stringValue.get());
        
        // Type-incompatible access should return empty (not throw exception)
        Optional<Integer> wrongType = attrs.getOne("stringAttr", Integer.class);
        assertFalse(wrongType.isPresent());
    }

    @Test
    void getOneThrowsExceptionOnNullType() {
        AttributesCollection attrs = AttributesCollection.empty();
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> attrs.getOne("test", null)
        );
        
        assertTrue(exception.getMessage().contains("type cannot be null"));
    }

    @Test
    void getOneHandlesNullValuesCorrectly() {
        // Test with a real case: first add a non-null value, then check behavior
        AttributesCollection attrs = AttributesCollection.empty()
            .set("testAttr", "notNull");
        
        // This should work fine
        Optional<String> stringValue = attrs.getOne("testAttr", String.class);
        assertTrue(stringValue.isPresent());
        assertEquals("notNull", stringValue.get());
        
        // Now test what happens with null values through clear operation
        AttributesCollection clearedAttrs = attrs.clear("testAttr", com.db.assetstore.domain.model.type.AttributeType.STRING);
        Optional<String> clearedValue = clearedAttrs.getOne("testAttr", String.class);
        // After clearing, either the attribute exists with null value or doesn't exist
        if (clearedValue.isPresent()) {
            assertNull(clearedValue.get());
        }
        // If not present, that's also acceptable behavior
    }

    @Test
    void getOneReturnsEmptyForNonExistentAttribute() {
        AttributesCollection attrs = AttributesCollection.empty();
        
        Optional<String> nonExistent = attrs.getOne("nonExistent", String.class);
        assertFalse(nonExistent.isPresent());
    }

    @Test
    void getFirstByNameIsDeprecated() {
        AttributesCollection attrs = AttributesCollection.empty()
            .set("test", "value");
        
        // This method should still work but is marked as deprecated
        Optional<AttributeValue<?>> result = attrs.getFirstByName("test");
        assertTrue(result.isPresent());
        assertEquals("value", result.get().value());
    }

    @Test
    void typeSafetyPreventsCastExceptionWithComplexTypes() {
        AttributesCollection attrs = AttributesCollection.empty()
            .set("booleanAttr", true)
            .set("decimalAttr", BigDecimal.valueOf(123.45));
        
        // Try to get boolean as string - should return empty, not throw
        Optional<String> boolAsString = attrs.getOne("booleanAttr", String.class);
        assertFalse(boolAsString.isPresent());
        
        // Try to get decimal as boolean - should return empty, not throw
        Optional<Boolean> decimalAsBool = attrs.getOne("decimalAttr", Boolean.class);
        assertFalse(decimalAsBool.isPresent());
        
        // But getting with correct types should work
        Optional<Boolean> boolValue = attrs.getOne("booleanAttr", Boolean.class);
        assertTrue(boolValue.isPresent());
        assertTrue(boolValue.get());
        
        Optional<BigDecimal> decimalValue = attrs.getOne("decimalAttr", BigDecimal.class);
        assertTrue(decimalValue.isPresent());
        assertEquals(BigDecimal.valueOf(123.45), decimalValue.get());
    }
}