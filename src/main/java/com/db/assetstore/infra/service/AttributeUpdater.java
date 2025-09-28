package com.db.assetstore.infra.service;

import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributeValueVisitor;
import com.db.assetstore.domain.model.type.AttributeType;
import com.db.assetstore.infra.jpa.AttributeEntity;

import java.math.BigDecimal;
import java.time.Instant;

public final class AttributeUpdater {
    private AttributeUpdater() {}

    public static void apply(AttributeEntity e, AttributeValue<?> av) {
        e.addHistory(Instant.now());
        av.accept(new AttributeValueVisitor<AttributeEntity>() {
            @Override public AttributeEntity visitString(String v, String name) {
                e.setValueStr(v);
                return e;
            }
            @Override public AttributeEntity visitDecimal(BigDecimal v, String name) {
                e.setValueNum(v);
                return e;
            }
            @Override public AttributeEntity visitBoolean(Boolean v, String name) {
                e.setValueBool(v);
                return e;
            }

            @Override
            public AttributeEntity visitDate(Instant v, String name) {
                e.setValueDate(v);
                return e;
            }
        });
    }
}
