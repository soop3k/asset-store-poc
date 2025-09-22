package com.db.assetstore.domain.model.attribute;

import com.db.assetstore.domain.model.type.AttributeType;

import java.math.BigDecimal;
import java.time.Instant;

public record AttributeHistory(
        String assetId,
        String name,
        String valueStr,
        BigDecimal valueNum,
        Boolean valueBool,
        AttributeType valueType,
        Instant changedAt
) {
}
