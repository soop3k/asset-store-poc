package com.db.assetstore.domain.service.validation.rule;

import java.util.Set;
import java.util.stream.Collectors;

public class RuleViolationException extends AttributeValidationException {

    private final String rule;
    private final Set<String> attributes;

    public RuleViolationException(String rule, String attribute, String message) {
        this(rule, attribute == null ? Set.of() : Set.of(attribute), message);
    }

    public RuleViolationException(String rule, Set<String> attributes, String message) {
        super(message);
        this.rule = rule;
        this.attributes = attributes == null ? Set.of() : Set.copyOf(attributes);
    }

    public String rule() {
        return rule;
    }

    public Set<String> attributes() {
        return attributes;
    }

    @Override
    public String getMessage() {
        if (attributes.isEmpty()) {
            return super.getMessage();
        }
        String joined = attributes.stream().sorted().collect(Collectors.joining(", "));
        return String.format("[%s] %s", joined, super.getMessage());
    }
}

