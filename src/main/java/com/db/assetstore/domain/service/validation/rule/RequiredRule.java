package com.db.assetstore.domain.service.validation.rule;

import com.db.assetstore.domain.service.type.ConstraintDefinition;
import com.db.assetstore.domain.service.validation.ValidationMode;
import lombok.RequiredArgsConstructor;

import static org.springframework.util.ObjectUtils.isEmpty;

@RequiredArgsConstructor
public final class RequiredRule implements ValidationRule {

    private final String attributeName;

    @Override
    public ConstraintDefinition.Rule rule() {
        return ConstraintDefinition.Rule.REQUIRED;
    }

    @Override
    public boolean shouldValidate(AttributeValidationContext context, ValidationMode mode) {
        return mode.enforceRequiredForMissing() || !context.values().isEmpty();
    }

    @Override
    public void validate(AttributeValidationContext context) {
        if (context.values().isEmpty()) {
            throw new RuleViolationException(rule().name(), attributeName,
                    "Attribute is required", "non-null value", "<absent>");
        }
        for (var value : context.values()) {
            if (value == null || isEmpty(value.value())) {
                throw new RuleViolationException(rule().name(), attributeName,
                        "Attribute is required", "non-null value", null);
            }
        }
    }
}
