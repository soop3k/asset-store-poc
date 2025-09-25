package com.db.assetstore.domain.search;

import com.db.assetstore.domain.model.attribute.AttributeValue;
import org.springframework.lang.NonNull;

public final class Condition<T> {
    private final String attribute;
    private final Operator operator;
    private final AttributeValue<T> value;

    public Condition(@NonNull String attribute, @NonNull Operator operator, AttributeValue<T> value) {
        this.attribute = attribute;
        this.operator = operator;
        this.value = value;
    }

    public String attribute() { return attribute; }
    public Operator operator() { return operator; }
    public AttributeValue<T> value() { return value; }
}
