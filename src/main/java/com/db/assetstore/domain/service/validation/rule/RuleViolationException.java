package com.db.assetstore.domain.service.validation.rule;

import java.util.Set;

public class RuleViolationException extends AttributeValidationException {

    private final RuleViolation violation;

    public RuleViolationException(String rule, String attribute, String message) {
        this(rule, attribute, message, null, null);
    }

    public RuleViolationException(String rule,
                                  String attribute,
                                  String message,
                                  Object expected,
                                  Object actual) {
        this(rule, attribute == null ? Set.of() : Set.of(attribute), message, expected, actual);
    }

    public RuleViolationException(String rule, Set<String> attributes, String message) {
        this(rule, attributes, message, null, null);
    }

    public RuleViolationException(String rule,
                                  Set<String> attributes,
                                  String message,
                                  Object expected,
                                  Object actual) {
        super(message);
        this.violation = new RuleViolation(rule, attributes, message, expected, actual);
    }

    public String rule() {
        return violation.rule();
    }

    public Set<String> attributes() {
        return violation.attributes();
    }

    public RuleViolation violation() {
        return violation;
    }

    public Object expected() {
        return violation.expected();
    }

    public Object actual() {
        return violation.actual();
    }

    @Override
    public String getMessage() {
        return violation.format();
    }
}
