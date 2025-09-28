package com.db.assetstore.domain.model.type;

import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributeValueVisitor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AVDate(String name, Instant value)
        implements AttributeValue<Instant> {

    @Override public AttributeValue<Instant> withValue(Instant v) {
        return new AVDate(name, v);
    }

    @Override public AttributeType attributeType() {
        return AttributeType.DATE;
    }

    @Override public <R> R accept(AttributeValueVisitor<R> v) {
        return v.visitDate(value, name);
    }

    public static AVDate of(String name, Instant d) {
        return new AVDate(name, d);
    }
    public static AVDate of(String name, LocalDateTime d) {
        return new AVDate(name, d.toInstant(java.time.ZoneOffset.UTC));
    }
    public static AVDate of(String name, LocalDate d) {
        return new AVDate(name, Instant.ofEpochMilli(
                d.atStartOfDay().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()));
    }
}
