package com.db.assetstore.domain.model.type;

import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributeValueVisitor;

import java.math.BigDecimal;

public record AVBoolean(String name, Boolean value)
        implements AttributeValue<Boolean> {

    @Override public AttributeValue<Boolean> withValue(Boolean v) {
        return new AVBoolean(name, v);
    }

    @Override public AttributeType attributeType() {
        return AttributeType.BOOLEAN;
    }

    @Override public <R> R accept(AttributeValueVisitor<R> v) {
        return v.visitBoolean(value, name);
    }

    public static AVBoolean of(String name, Boolean b) {
        return new AVBoolean(name, b);
    }
}
