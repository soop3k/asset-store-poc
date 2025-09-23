package com.db.assetstore.domain.model.type;


import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributeValueVisitor;

import java.math.BigDecimal;

public record AVDecimal(String name, BigDecimal value)
        implements AttributeValue<BigDecimal> {
    @Override public AttributeValue<BigDecimal> withValue(BigDecimal v) { return new AVDecimal(name, v); }
    @Override public AttributeType attributeType() { return AttributeType.DECIMAL; }
    @Override public <R> R accept(AttributeValueVisitor<R> v) { return v.visitDecimal(value, name); }

    // Normalize numeric JSON output: remove trailing zeros so 3.0 becomes 3
    @Override public BigDecimal value() {
        return value == null ? null : value.stripTrailingZeros();
    }

    public static AVDecimal of(String name, Number n) {
        return new AVDecimal(name, n == null ? null : new BigDecimal(n.toString()));
    }
}
