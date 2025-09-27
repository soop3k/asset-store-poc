package com.db.assetstore.domain.service.validation.rule;

import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributeValueVisitor;
import com.db.assetstore.domain.model.type.AttributeType;
import com.db.assetstore.domain.service.type.AttributeDefinition;
import com.db.assetstore.domain.service.type.ConstraintDefinition;
public final class TypeRule implements ValidationRule {

    private final String attributeName;
    private final AttributeType expectedType;

    public TypeRule(AttributeDefinition definition) {
        this.attributeName = definition.name();
        this.expectedType = definition.attributeType();
    }

    @Override
    public ConstraintDefinition.Rule rule() {
        return ConstraintDefinition.Rule.TYPE;
    }

    @Override
    public void validate(AttributeValidationContext context) {
        for (var value : context.values()) {
            if (value == null) {
                continue;
            }
            if (value.attributeType() != expectedType) {
                throw new RuleViolationException(rule().name(), attributeName,
                        "Attribute type mismatch", expectedType, value.attributeType());
            }
            value.accept(new AttributeValueVisitor<Void>() {
                @Override
                public Void visitString(String v, String name) {
                    if (expectedType != AttributeType.STRING) {
                        throw mismatch(AttributeType.STRING, name);
                    }
                    return null;
                }

                @Override
                public Void visitDecimal(java.math.BigDecimal v, String name) {
                    if (expectedType != AttributeType.DECIMAL) {
                        throw mismatch(AttributeType.DECIMAL, name);
                    }
                    return null;
                }

                @Override
                public Void visitBoolean(Boolean v, String name) {
                    if (expectedType != AttributeType.BOOLEAN) {
                        throw mismatch(AttributeType.BOOLEAN, name);
                    }
                    return null;
                }

                private RuleViolationException mismatch(AttributeType actualType, String attribute) {
                    return new RuleViolationException(rule().name(), attribute,
                            "Attribute type mismatch", expectedType, actualType);
                }
            });
        }
    }
}
