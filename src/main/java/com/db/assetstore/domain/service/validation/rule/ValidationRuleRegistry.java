package com.db.assetstore.domain.service.validation.rule;

import com.db.assetstore.domain.service.type.ConstraintDefinition;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public final class ValidationRuleRegistry {

    private final Map<ConstraintDefinition.Rule, ValidationRule> rules;

    public ValidationRuleRegistry(List<ValidationRule> availableRules) {
        Map<ConstraintDefinition.Rule, ValidationRule> map = new EnumMap<>(ConstraintDefinition.Rule.class);
        for (ValidationRule rule : availableRules) {
            if (rule instanceof CustomValidationRule) {
                continue;
            }
            map.put(rule.rule(), rule);
        }
        this.rules = Map.copyOf(map);
    }

    public ValidationRule get(ConstraintDefinition.Rule rule) {
        return rules.get(rule);
    }
}
