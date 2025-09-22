package com.db.assetstore.domain.search;

import java.util.Objects;

public final class Condition {
    private final String attribute;
    private final Operator operator;
    private final Object value;

    public Condition(String attribute, Operator operator, Object value) {
        this.attribute = Objects.requireNonNull(attribute);
        this.operator = Objects.requireNonNull(operator);
        this.value = value;
    }

    public String attribute() { return attribute; }
    public Operator operator() { return operator; }
    public Object value() { return value; }
}
