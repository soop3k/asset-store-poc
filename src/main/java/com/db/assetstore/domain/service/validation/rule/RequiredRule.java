package com.db.assetstore.domain.service.validation.rule;

import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.service.type.ConstraintDefinition;
import org.springframework.stereotype.Component;

@Component
public final class RequiredRule implements ValidationRule {

    @Override
    public ConstraintDefinition.Rule rule() {
        return ConstraintDefinition.Rule.REQUIRED;
    }

    @Override
    public void validate(AttributeValidationContext context) {
        ConstraintDefinition constraint = context.constraint();
        if (constraint == null) {
            return;
        }
        if (context.values().isEmpty()) {
            throw new RuleViolationException(rule().name(), context.definition().name(),
                    "Attribute is required");
        }
        for (AttributeValue<?> value : context.values()) {
            if (value == null || value.value() == null) {
                throw new RuleViolationException(rule().name(), context.definition().name(),
                        "Attribute is required");
            }
            Object raw = value.value();
            if (raw instanceof CharSequence sequence && sequence.toString().isBlank()) {
                throw new RuleViolationException(rule().name(), context.definition().name(),
                        "Attribute must not be blank");
            }
        }
    }
}
