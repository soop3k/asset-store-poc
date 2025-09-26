package com.db.assetstore.domain.service.validation.rule;

import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributeValueVisitor;
import com.db.assetstore.domain.service.type.ConstraintDefinition;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Component
public final class EnumRule implements ValidationRule {

    @Override
    public ConstraintDefinition.Rule rule() {
        return ConstraintDefinition.Rule.ENUM;
    }

    @Override
    public void validate(AttributeValidationContext context) {
        ConstraintDefinition constraint = context.constraint();
        if (constraint == null) {
            throw new AttributeValidationException("ENUM rule requires constraint definition");
        }
        AllowedValues allowed = parseAllowed(constraint.value());
        for (AttributeValue<?> value : context.values()) {
            if (value == null || value.value() == null) {
                continue;
            }
            boolean match = value.accept(new AttributeValueVisitor<Boolean>() {
                @Override
                public Boolean visitString(String v, String name) {
                    return allowed.strings.contains(v);
                }

                @Override
                public Boolean visitDecimal(BigDecimal v, String name) {
                    return allowed.decimals.contains(v);
                }

                @Override
                public Boolean visitBoolean(Boolean v, String name) {
                    return allowed.booleans.contains(v);
                }
            });
            if (!match) {
                throw new RuleViolationException(rule().name(), context.definition().name(),
                        "Value '" + value.value() + "' is not allowed");
            }
        }
    }

    private AllowedValues parseAllowed(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new AttributeValidationException("ENUM rule requires allowed values");
        }
        String[] tokens = raw.split(",");
        Set<String> strings = new HashSet<>();
        Set<BigDecimal> decimals = new HashSet<>();
        Set<Boolean> booleans = new HashSet<>();
        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (isBoolean(trimmed)) {
                booleans.add(Boolean.parseBoolean(trimmed));
            } else if (isNumeric(trimmed)) {
                decimals.add(new BigDecimal(trimmed));
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
    }
}
