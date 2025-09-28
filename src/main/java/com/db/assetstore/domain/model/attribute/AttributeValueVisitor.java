package com.db.assetstore.domain.model.attribute;


import java.math.BigDecimal;
import java.time.Instant;

public interface AttributeValueVisitor<R> {
    R visitString(String v, String name);
    R visitDecimal(BigDecimal v, String name);
    R visitBoolean(Boolean v, String name);
    R visitDate(Instant v, String name);
}

