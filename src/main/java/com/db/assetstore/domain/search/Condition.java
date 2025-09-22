package com.db.assetstore.domain.search;

import com.db.assetstore.domain.model.attribute.AttributeValue;

import java.util.Objects;

public final class Condition<T> {
    private final String attribute;
    private final Operator operator;
    private final AttributeValue<T> value;

    public Condition(String attribute, Operator operator, AttributeValue<T> value) {
        this.attribute = Objects.requireNonNull(attribute);
        this.operator = Objects.requireNonNull(operator);
        this.value = value;
    }

    public String attribute() { return attribute; }
    public Operator operator() { return operator; }
    public AttributeValue<T> value() { return value; }
}
