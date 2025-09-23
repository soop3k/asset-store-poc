package com.db.assetstore.infra.service;

import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributeValueVisitor;
import com.db.assetstore.domain.model.type.AttributeType;

import java.math.BigDecimal;
import java.util.Objects;

public final class AttributeComparator {
    private AttributeComparator() {}

    // Comparator must not modify data. Operates on domain AttributeValue and uses visitor for type-dispatch.
    public static boolean checkforUpdates(AttributeValue<?> existing, AttributeValue<?> incoming) {
        if (existing == null || incoming == null) return existing != incoming;
        return incoming.accept(new AttributeValueVisitor<>() {
            @Override
            public Boolean visitString(String v, String name) {
                if (existing.attributeType() != AttributeType.STRING) return true;
                String a = (String) existing.value();
                return !Objects.equals(a, v);
            }

            @Override
            public Boolean visitDecimal(BigDecimal v, String name) {
                if (existing.attributeType() != AttributeType.DECIMAL) return true;
                BigDecimal a = (BigDecimal) existing.value();
                if (a == null || v == null) return a != v;
                return a.compareTo(v) != 0;
            }

            @Override
            public Boolean visitBoolean(Boolean v, String name) {
                if (existing.attributeType() != AttributeType.BOOLEAN) return true;
                Boolean a = (Boolean) existing.value();
                return !Objects.equals(a, v);
            }
        });
    }
}
