package com.db.assetstore.domain.service.validation.rule;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Captures the details of a validation failure so callers can inspect
 * the violated rule, affected attributes, and any expected/actual context.
 */
public final class RuleViolation {

    private final String rule;
    private final Set<String> attributes;
    private final String message;
    private final Object expected;
    private final Object actual;

    public RuleViolation(String rule,
                         Set<String> attributes,
                         String message,
                         Object expected,
                         Object actual) {
        this.rule = Objects.requireNonNull(rule, "rule must not be null");
        this.attributes = attributes == null ? Set.of() : Set.copyOf(attributes);
        this.message = message == null ? "" : message;
        this.expected = expected;
        this.actual = actual;
    }

    public static RuleViolation forAttribute(String rule,
                                             String attribute,
                                             String message,
                                             Object expected,
                                             Object actual) {
        var attrs = attribute == null ? Set.<String>of() : Set.of(attribute);
        return new RuleViolation(rule, attrs, message, expected, actual);
    }

    public String rule() {
        return rule;
    }

    public Set<String> attributes() {
        return attributes;
    }

    public String message() {
        return message;
    }

    public Object expected() {
        return expected;
    }

    public Object actual() {
        return actual;
    }

    /**
     * Human readable representation that includes attribute names and
     * expected/actual context when available.
     */
    public String format() {
        var prefix = attributes.isEmpty()
                ? ""
                : String.format("[%s] ", attributes.stream().sorted().collect(Collectors.joining(", ")));
        var baseMessage = message == null || message.isBlank() ? "Validation failed" : message;
        if (expected == null && actual == null) {
            return prefix + baseMessage;
        }
        return prefix + baseMessage + String.format(" (expected: %s, actual: %s)", expected, actual);
    }
}
