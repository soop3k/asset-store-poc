package com.db.assetstore.domain.model.type;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum AttributeType {
    STRING(String.class),
    DECIMAL(BigDecimal.class),
    BOOLEAN(Boolean.class);

    private final Class<?> javaType;
    AttributeType(Class<?> javaType) { this.javaType = javaType; }
}