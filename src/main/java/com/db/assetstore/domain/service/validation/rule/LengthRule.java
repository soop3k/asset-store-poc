package com.db.assetstore.domain.service.validation.rule;

import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributeValueVisitor;
import com.db.assetstore.domain.service.type.ConstraintDefinition;
import org.springframework.stereotype.Component;

@Component
public final class LengthRule implements ValidationRule {

    @Override
    public ConstraintDefinition.Rule rule() {
        return ConstraintDefinition.Rule.LENGTH;
    }

    @Override
    public void validate(AttributeValidationContext context) {
        ConstraintDefinition constraint = context.constraint();
        if (constraint == null) {
            throw new AttributeValidationException("LENGTH rule requires constraint definition");
        }
        int max = parseMax(constraint.value());
        for (AttributeValue<?> value : context.values()) {
            if (value == null || value.value() == null) {
                continue;
            }
            value.accept(new AttributeValueVisitor<Void>() {
                @Override
                public Void visitString(String v, String name) {
                    ensureLength(name, v == null ? 0 : v.length(), max, context);
                    return null;
                }

                @Override
                public Void visitDecimal(java.math.BigDecimal v, String name) {
                    String text = v == null ? "" : v.toPlainString();
                    ensureLength(name, text.length(), max, context);
                    return null;
                }

                @Override
                public Void visitBoolean(Boolean v, String name) {
                    String text = v == null ? "" : v.toString();
                    ensureLength(name, text.length(), max, context);
                    return null;
                }
            });
        }
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

    private void ensureLength(String name, int length, int max, AttributeValidationContext context) {
        if (length > max) {
            throw new RuleViolationException(rule().name(), name,
                    "Length must be less than or equal to " + max);
        }
    }
}
