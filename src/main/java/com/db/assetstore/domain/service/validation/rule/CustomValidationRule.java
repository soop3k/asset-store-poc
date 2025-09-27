package com.db.assetstore.domain.service.validation.rule;

/**
 * Marker interface for custom validation rules that are resolved dynamically
 * through dependency injection. Each implementation must expose a unique name
 * that is referenced from constraint definitions.
 */
public interface CustomValidationRule extends ValidationRule {

    /**
     * Name used in {@link com.db.assetstore.domain.service.type.ConstraintDefinition}
     * values to reference this rule.
     */
    String name();
}
