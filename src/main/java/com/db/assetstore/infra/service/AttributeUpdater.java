package com.db.assetstore.infra.service;

import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributeValueVisitor;
import com.db.assetstore.domain.model.type.AttributeType;
import com.db.assetstore.infra.jpa.AttributeEntity;

import java.math.BigDecimal;
import java.time.Instant;

public final class AttributeUpdater {
    private AttributeUpdater() {}

    public static void apply(AttributeEntity e, AttributeValue<?> av) {
        if (e == null || av == null) {
            return;
        }
        e.addHistory(Instant.now());
        av.accept(new AttributeValueVisitor<>() {
            @Override public Void visitString(String v, String name) {
                e.setValueType(AttributeType.STRING);
                e.setValueStr(v);
                return null;
            }
            @Override public Void visitDecimal(BigDecimal v, String name) {
                e.setValueType(AttributeType.DECIMAL);
                e.setValueNum(v);
                return null;
            }
            @Override public Void visitBoolean(Boolean v, String name) {
                e.setValueType(AttributeType.BOOLEAN);
                e.setValueBool(v);
                return null;
            }
        });
    }
}
