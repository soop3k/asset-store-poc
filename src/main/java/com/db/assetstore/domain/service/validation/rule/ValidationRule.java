package com.db.assetstore.domain.service.validation.rule;

import com.db.assetstore.domain.service.type.ConstraintDefinition;

public interface ValidationRule {

    ConstraintDefinition.Rule rule();

    void validate(AttributeValidationContext context);
}
