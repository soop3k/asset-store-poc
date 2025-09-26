package com.db.assetstore.domain.service.validation.rule;

public final class RequiredRule implements ValidationRule {

    private final String attributeName;

    public RequiredRule(String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public ConstraintDefinition.Rule rule() {
        return ConstraintDefinition.Rule.REQUIRED;
    }

    @Override
    public void validate(AttributeValidationContext context) {
        if (context.values().isEmpty()) {
            throw new RuleViolationException(rule().name(), attributeName,
                    "Attribute is required");
        }
        for (var value : context.values()) {
            if (value == null || value.value() == null) {
                throw new RuleViolationException(rule().name(), attributeName,
                        "Attribute is required");
            }
            var raw = value.value();
            if (raw instanceof CharSequence sequence && sequence.toString().isBlank()) {
                throw new RuleViolationException(rule().name(), attributeName,
                        "Attribute must not be blank");
            }
        }
    }
}
