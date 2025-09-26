package com.db.assetstore.domain.service.validation.rule;

import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributeValueVisitor;
import com.db.assetstore.domain.model.type.AttributeType;
import com.db.assetstore.domain.service.type.AttributeDefinition;
import com.db.assetstore.domain.service.type.ConstraintDefinition;
import org.springframework.stereotype.Component;

@Component
public final class TypeRule implements ValidationRule {

    @Override
    public ConstraintDefinition.Rule rule() {
        return ConstraintDefinition.Rule.TYPE;
    }

    @Override
    public void validate(AttributeValidationContext context) {
        AttributeDefinition definition = context.definition();
        AttributeType expected = definition.attributeType();
        for (AttributeValue<?> value : context.values()) {
            if (value == null) {
                continue;
            }
            if (value.attributeType() != expected) {
                throw new RuleViolationException(rule().name(), definition.name(),
                        "Attribute type mismatch. Expected " + expected + " but got " + value.attributeType());
            }
            value.accept(new AttributeValueVisitor<Void>() {
                @Override
                public Void visitString(String v, String name) {
                    if (expected != AttributeType.STRING) {
                        throw mismatch(expected, AttributeType.STRING, name);
                    }
                    return null;
                }

                @Override
                public Void visitDecimal(java.math.BigDecimal v, String name) {
                    if (expected != AttributeType.DECIMAL) {
                        throw mismatch(expected, AttributeType.DECIMAL, name);
                    }
                    return null;
                }

                @Override
                public Void visitBoolean(Boolean v, String name) {
                    if (expected != AttributeType.BOOLEAN) {
                        throw mismatch(expected, AttributeType.BOOLEAN, name);
                    }
                    return null;
                }

                private RuleViolationException mismatch(AttributeType expectedType,
                                                        AttributeType actualType,
                                                        String attributeName) {
                    return new RuleViolationException(rule().name(), attributeName,
                            "Attribute type mismatch. Expected " + expectedType + " but got " + actualType);
                }
            });
        }
    }
}
