package com.db.assetstore.domain.model.attribute;


import java.math.BigDecimal;

public interface AttributeValueVisitor<R> {
    R visitString(String v, String name);
    R visitDecimal(BigDecimal v, String name);
    R visitBoolean(Boolean v, String name);
}

