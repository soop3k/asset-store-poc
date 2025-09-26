package com.db.assetstore.domain.service.validation.rule;

import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributeValueVisitor;
import com.db.assetstore.domain.service.type.ConstraintDefinition;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public final class MinMaxRule implements ValidationRule {

    @Override
    public ConstraintDefinition.Rule rule() {
        return ConstraintDefinition.Rule.MIN_MAX;
    }

    @Override
    public void validate(AttributeValidationContext context) {
        ConstraintDefinition constraint = context.constraint();
        if (constraint == null) {
            throw new AttributeValidationException("MIN_MAX rule requires constraint definition");
        }
        Bounds bounds = parseBounds(constraint.value());
        for (AttributeValue<?> value : context.values()) {
            if (value == null || value.value() == null) {
                continue;
            }
            BigDecimal number = toDecimal(value);
            if (bounds.min != null && number.compareTo(bounds.min) < 0) {
                throw new RuleViolationException(rule().name(), context.definition().name(),
                        "Value " + number + " is less than minimum " + bounds.min);
            }
            if (bounds.max != null && number.compareTo(bounds.max) > 0) {
                throw new RuleViolationException(rule().name(), context.definition().name(),
                        "Value " + number + " exceeds maximum " + bounds.max);
            }
        }
    }

    private Bounds parseBounds(String raw) {
        if (raw == null) {
            throw new AttributeValidationException("MIN_MAX rule requires bounds");
        }
        String[] parts = raw.split(",", -1);
        BigDecimal min = parseDecimal(parts, 0);
        BigDecimal max = parseDecimal(parts, 1);
        if (min == null && max == null) {
            throw new AttributeValidationException("MIN_MAX rule requires bounds");
        }
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new AttributeValidationException("min must be less than or equal to max");
        }
        return new Bounds(min, max);
    }

    private BigDecimal parseDecimal(String[] parts, int index) {
        if (index >= parts.length) {
            return null;
        }
        String token = parts[index].trim();
        if (token.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(token);
        } catch (NumberFormatException ex) {
            throw new AttributeValidationException("MIN_MAX rule requires numeric values", ex);
        }
    }

    private BigDecimal toDecimal(AttributeValue<?> value) {
        return value.accept(new AttributeValueVisitor<BigDecimal>() {
            @Override
            public BigDecimal visitString(String v, String name) {
                try {
                    return new BigDecimal(v);
                } catch (NumberFormatException ex) {
                    throw new RuleViolationException(rule().name(), name,
                            "Value '" + v + "' is not numeric");
                }
            }

            @Override
            public BigDecimal visitDecimal(BigDecimal v, String name) {
                return v;
            }

            @Override
            public BigDecimal visitBoolean(Boolean v, String name) {
                throw new RuleViolationException(rule().name(), name,
                        "Boolean value not allowed for numeric constraint");
            }
        });
    }

    private record Bounds(BigDecimal min, BigDecimal max) {
    }
}
