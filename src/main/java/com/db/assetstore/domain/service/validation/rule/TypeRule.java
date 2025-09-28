package com.db.assetstore.domain.service.validation.rule;

import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributeValueVisitor;
import com.db.assetstore.domain.model.type.AttributeType;
import com.db.assetstore.domain.service.type.AttributeDefinition;
import com.db.assetstore.domain.service.type.ConstraintDefinition;

import java.time.Instant;

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
            if (value.attributeType() != expectedType) {
                throw new RuleViolationException(rule().name(), attributeName,
                        "Attribute type mismatch", expectedType, value.attributeType());
            }
        }
    }
}
