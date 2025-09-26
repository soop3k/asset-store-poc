package com.db.assetstore.infra.service;

import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributeValueVisitor;
import com.db.assetstore.domain.model.type.AttributeType;

import java.math.BigDecimal;
import java.util.Objects;

public final class AttributeComparator {
    private AttributeComparator() {}

    public static boolean checkforUpdates(AttributeValue<?> existing, AttributeValue<?> incoming) {
        if (existing == null || incoming == null) {
            return existing != incoming;
        }
        return incoming.accept(new AttributeValueVisitor<>() {
            @Override
            public Boolean visitString(String v, String name) {
                String a = (String) existing.value();
                return !Objects.equals(a, v);
            }

            @Override
            public Boolean visitDecimal(BigDecimal v, String name) {
                BigDecimal a = (BigDecimal) existing.value();
                if (a == null || v == null) {
                    return a != v;
                }
                return a.compareTo(v) != 0;
            }

            @Override
            public Boolean visitBoolean(Boolean v, String name) {
                Boolean a = (Boolean) existing.value();
                return !Objects.equals(a, v);
            }
        });
    }
}
