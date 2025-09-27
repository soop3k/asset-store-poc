package com.db.assetstore.domain.service.validation.rule;

import com.db.assetstore.domain.model.attribute.AttributeValueVisitor;
import com.db.assetstore.domain.service.type.ConstraintDefinition;

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
        forEachPresentValue(context, value ->
                value.accept(new AttributeValueVisitor<Void>() {
                    @Override
                    public Void visitString(String v, String name) {
                        ensureLength(v == null ? 0 : v.length());
                        return null;
                    }

                    @Override
                    public Void visitDecimal(java.math.BigDecimal v, String name) {
                        var text = v == null ? "" : v.toPlainString();
                        ensureLength(text.length());
                        return null;
                    }

                    @Override
                    public Void visitBoolean(Boolean v, String name) {
                        var text = v == null ? "" : v.toString();
                        ensureLength(text.length());
                        return null;
                    }
                }));
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

    private void ensureLength(int length) {
        if (length > max) {
            throw new RuleViolationException(rule().name(), attributeName,
                    "Length exceeds the maximum", "<=" + max, length);
        }
    }
}
