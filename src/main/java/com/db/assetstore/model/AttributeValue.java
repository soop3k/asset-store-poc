package com.db.assetstore.model;

import java.util.Objects;

public final class AttributeValue<T> {
    private final String name;
    private final T value;
    private final Class<T> type;

    public AttributeValue(String name, T value, Class<T> type) {
        this.name = Objects.requireNonNull(name);
        this.value = value;
        this.type = Objects.requireNonNull(type);
    }

    public String name() { return name; }
    public T value() { return value; }
    public Class<T> type() { return type; }
}
