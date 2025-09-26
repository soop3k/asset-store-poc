package com.db.assetstore.domain.service.validation.rule;

import com.db.assetstore.domain.service.type.ConstraintDefinition;
import org.springframework.stereotype.Component;

@Component
public final class CustomRule implements ValidationRule {

    private final CustomValidationRuleRegistry registry;

    public CustomRule(CustomValidationRuleRegistry registry) {
        this.registry = registry;
    }

    @Override
    public ConstraintDefinition.Rule rule() {
        return ConstraintDefinition.Rule.CUSTOM;
    }

    @Override
    public void validate(AttributeValidationContext context) {
        ConstraintDefinition constraint = context.constraint();
        if (constraint == null) {
            throw new AttributeValidationException("CUSTOM rule requires constraint definition");
        }
        String ruleName = constraint.value();
        CustomValidationRule delegate = registry.get(ruleName);
        if (delegate == null) {
            throw new AttributeValidationException("Custom validation rule not registered: " + ruleName);
        }
        delegate.validate(context);
    }
}
