package com.db.assetstore.domain.service.validation.rule;

import com.db.assetstore.domain.service.type.ConstraintDefinition;

public final class CustomRule implements ValidationRule {

    private final CustomValidationRule delegate;

    public CustomRule(CustomValidationRule delegate) {
        this.delegate = delegate;
    }

    @Override
    public ConstraintDefinition.Rule rule() {
        return ConstraintDefinition.Rule.CUSTOM;
    }

    @Override
    public void validate(AttributeValidationContext context) {
        delegate.validate(context);
    }
}
