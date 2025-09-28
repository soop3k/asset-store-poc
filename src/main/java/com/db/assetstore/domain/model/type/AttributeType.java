package com.db.assetstore.domain.model.type;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
public enum AttributeType {
    STRING(String.class),
    DECIMAL(BigDecimal.class),
    BOOLEAN(Boolean.class),
    DATE(Instant.class);

    private final Class<?> javaType;
    AttributeType(Class<?> javaType) { this.javaType = javaType; }
}