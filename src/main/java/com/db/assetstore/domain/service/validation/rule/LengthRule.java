package com.db.assetstore.domain.service.validation.rule;

import com.db.assetstore.domain.model.attribute.AttributeValueVisitor;
import com.db.assetstore.domain.service.type.ConstraintDefinition;

import java.math.BigDecimal;
import java.time.Instant;

public final class LengthRule implements ValidationRule {

    private final String attributeName;
    private final int max;

    public LengthRule(String attributeName, String rawMax) {
        this.attributeName = attributeName;
        this.max = parseMax(rawMax);
    }

    @Override
    public ConstraintDefinition.Rule rule() {
        return ConstraintDefinition.Rule.LENGTH;
    }

    @Override
    public void validate(AttributeValidationContext context) {
        forEachPresentValue(context, value -> {
            boolean tooLong = value.accept(new AttributeValueVisitor<>() {
                @Override
                public Boolean visitString(String v, String name) {
                    return ensureLength(v == null ? 0 : v.length());
                }

                @Override
                public Boolean visitDecimal(BigDecimal v, String name) {
                    var text = v == null ? "" : v.toPlainString();
                    return ensureLength(text.length());
                }

                @Override
                public Boolean visitBoolean(Boolean v, String name) {
                    var text = v == null ? "" : v.toString();
                    return ensureLength(text.length());
                }

                @Override
                public Boolean visitDate(Instant v, String name) {
                    return false;
                }
            });

            if (tooLong) {
                throw new RuleViolationException(rule().name(), attributeName,
                        "Length exceeds the maximum");
            }
        });
    }

    private int parseMax(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new AttributeValidationException("LENGTH rule requires max value");
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            throw new AttributeValidationException("LENGTH rule requires numeric max value", ex);
        }
    }

    private boolean ensureLength(int length) {
        return length > max;
    }
}
