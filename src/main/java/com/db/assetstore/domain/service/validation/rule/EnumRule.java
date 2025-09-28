package com.db.assetstore.domain.service.validation.rule;

import com.db.assetstore.domain.model.attribute.AttributeValueVisitor;
import com.db.assetstore.domain.service.type.ConstraintDefinition;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public final class EnumRule implements ValidationRule {

    private final String attributeName;
    private final AllowedValues allowedValues;

    public EnumRule(String attributeName, String rawValues) {
        this.attributeName = attributeName;
        this.allowedValues = parseAllowed(rawValues);
    }

    @Override
    public ConstraintDefinition.Rule rule() {
        return ConstraintDefinition.Rule.ENUM;
    }

    @Override
    public void validate(AttributeValidationContext context) {
        forEachPresentValue(context, value -> {
            var match = value.accept(new AttributeValueVisitor<Boolean>() {
                @Override
                public Boolean visitString(String v, String name) {
                    return allowedValues.strings.contains(v);
                }

                @Override
                public Boolean visitDecimal(BigDecimal v, String name) {
                    return allowedValues.matchesDecimal(v);
                }

                @Override
                public Boolean visitBoolean(Boolean v, String name) {
                    return allowedValues.booleans.contains(v);
                }

                @Override
                public Boolean visitDate(Instant v, String name) { return false; }
            });

            if (!match) {
                throw new RuleViolationException(rule().name(), attributeName,
                        "Value is not in the allowed list", allowedValues.describe(), value.value());
            }
        });
    }

    private AllowedValues parseAllowed(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new AttributeValidationException("ENUM rule requires allowed values");
        }
        var tokens = raw.split(",");
        var strings = new HashSet<String>();
        var decimals = new HashSet<BigDecimal>();
        var booleans = new HashSet<Boolean>();
        for (var token : tokens) {
            var trimmed = token.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (isBoolean(trimmed)) {
                booleans.add(Boolean.parseBoolean(trimmed));
            } else if (isNumeric(trimmed)) {
                decimals.add(new BigDecimal(trimmed).stripTrailingZeros());
            } else {
                strings.add(trimmed);
            }
        }
        if (strings.isEmpty() && decimals.isEmpty() && booleans.isEmpty()) {
            throw new AttributeValidationException("ENUM rule requires allowed values");
        }
        return new AllowedValues(strings, decimals, booleans);
    }

    private boolean isBoolean(String value) {
        return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
    }

    private boolean isNumeric(String value) {
        try {
            new BigDecimal(value);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private record AllowedValues(Set<String> strings,
                                 Set<BigDecimal> decimals,
                                 Set<Boolean> booleans) {

        boolean matchesDecimal(BigDecimal candidate) {
            return decimals.stream().anyMatch(value -> value.compareTo(candidate) == 0);
        }

        String describe() {
            var parts = new ArrayList<String>();
            if (!strings.isEmpty()) {
                parts.add("strings=" + strings);
            }
            if (!decimals.isEmpty()) {
                parts.add("decimals=" + decimals);
            }
            if (!booleans.isEmpty()) {
                parts.add("booleans=" + booleans);
            }
            return parts.isEmpty() ? "[]" : String.join("; ", parts);
        }
    }
}
