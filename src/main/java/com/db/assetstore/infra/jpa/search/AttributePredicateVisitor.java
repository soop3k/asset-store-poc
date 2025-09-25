package com.db.assetstore.infra.jpa.search;

import com.db.assetstore.domain.model.attribute.AttributeValueVisitor;
import com.db.assetstore.domain.search.Condition;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;

/**
 * Domain-level builder that converts an attribute Condition into a JPA Criteria Predicate
 * without depending on infra entities. It assumes the attribute value columns are named
 * valueStr, valueNum and valueBool on the joined attribute path.
 */
public final class AttributePredicateVisitor {

    private AttributePredicateVisitor() {}

    public static <T> Predicate build(CriteriaBuilder cb, From<?, ?> attr, Condition<T> cond) {
        var op = cond.operator();

        AttributeValueVisitor<Predicate> v = new AttributeValueVisitor<>() {
            @Override
            public Predicate visitString(String s, String name) {
                Path<String> p = attr.get("valueStr");
                return switch (op) {
                    case EQ   -> (s == null) ? cb.isNull(p) : cb.equal(cb.lower(p), s.toLowerCase());
                    case LIKE -> (s == null) ? unsupported("LIKE on null STRING") : cb.like(cb.lower(p), ensureLikePattern(s));
                    case GT, LT -> unsupported("STRING");
                };
            }

            @Override
            public Predicate visitDecimal(BigDecimal num, String name) {
                Path<BigDecimal> p = attr.get("valueNum");
                return switch (op) {
                    case EQ -> (num == null) ? cb.isNull(p) : cb.equal(p, num);
                    case GT -> (num == null) ? unsupported("GT on null DECIMAL") : cb.greaterThan(p, num);
                    case LT -> (num == null) ? unsupported("LT on null DECIMAL") : cb.lessThan(p, num);
                    case LIKE -> unsupported("LIKE on DECIMAL");
                };
            }

            @Override
            public Predicate visitBoolean(Boolean b, String name) {
                Path<Boolean> p = attr.get("valueBool");
                return switch (op) {
                    case EQ -> (b == null) ? cb.isNull(p) : cb.equal(p, b);
                    case GT, LT, LIKE -> unsupported("BOOLEAN");
                };
            }

            private Predicate unsupported(String kind) {
                throw new IllegalArgumentException("Operator " + op + " unsupported for " + kind + " (attr=" + cond.attribute() + ")");
            }
        };

        return cond.value().accept(v);
    }

    private static String ensureLikePattern(String s) {
        if (s == null) {
            return null;
        }
        String norm = s.toLowerCase();
        return (norm.indexOf('%') >= 0 || norm.indexOf('_') >= 0) ? norm : "%" + norm + "%";
    }
}
