package com.db.assetstore.domain.service.validation.rule;

import com.db.assetstore.domain.model.attribute.AttributeValueVisitor;
import com.db.assetstore.domain.service.type.ConstraintDefinition;

import java.math.BigDecimal;

public final class MinMaxRule implements ValidationRule {

    private final String attributeName;
    private final BigDecimal min;
    private final BigDecimal max;

    public MinMaxRule(String attributeName, String rawBounds) {
        this.attributeName = attributeName;
        var bounds = parseBounds(rawBounds);
        this.min = bounds.min;
        this.max = bounds.max;
    }

    @Override
    public ConstraintDefinition.Rule rule() {
        return ConstraintDefinition.Rule.MIN_MAX;
    }

    @Override
    public void validate(AttributeValidationContext context) {
        for (var value : context.values()) {
            if (value == null || value.value() == null) {
                continue;
            }
            var number = toDecimal(value);
            if (min != null && number.compareTo(min) < 0) {
                throw new RuleViolationException(rule().name(), attributeName,
                        "Value " + number + " is less than minimum " + min);
            }
            if (max != null && number.compareTo(max) > 0) {
                throw new RuleViolationException(rule().name(), attributeName,
                        "Value " + number + " exceeds maximum " + max);
            }
        }
    }

    private Bounds parseBounds(String raw) {
        if (raw == null) {
            throw new AttributeValidationException("MIN_MAX rule requires bounds");
        }
        var parts = raw.split(",", -1);
        var min = parseDecimal(parts, 0);
        var max = parseDecimal(parts, 1);
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
        var token = parts[index].trim();
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
