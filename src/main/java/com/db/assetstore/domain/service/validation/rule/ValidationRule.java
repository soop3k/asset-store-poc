package com.db.assetstore.domain.service.validation.rule;

import com.db.assetstore.domain.service.type.ConstraintDefinition;
import com.db.assetstore.domain.service.validation.ValidationMode;

public interface ValidationRule {

    ConstraintDefinition.Rule rule();

    default boolean shouldValidate(AttributeValidationContext context, ValidationMode mode) {
        return true;
    }

    void validate(AttributeValidationContext context);
}
