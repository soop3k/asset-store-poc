package com.db.assetstore.domain.service.validation.rule;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Raised when validation encounters one or more rule violations. The
 * collected violations expose detailed context that callers can inspect.
 */
public final class AttributeValidationErrorsException extends AttributeValidationException {

    private final List<RuleViolation> violations;

    public AttributeValidationErrorsException(List<RuleViolation> violations) {
        super(buildMessage(violations));
        if (violations == null || violations.isEmpty()) {
            throw new IllegalArgumentException("violations must not be empty");
        }
        this.violations = List.copyOf(violations);
    }

    public List<RuleViolation> violations() {
        return violations;
    }

    private static String buildMessage(List<RuleViolation> violations) {
        Objects.requireNonNull(violations, "violations must not be null");
        return violations.stream()
                .map(RuleViolation::format)
                .collect(Collectors.joining("; "));
    }
}
