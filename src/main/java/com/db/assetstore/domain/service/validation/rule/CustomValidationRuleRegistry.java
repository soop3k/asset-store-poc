package com.db.assetstore.domain.service.validation.rule;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public final class CustomValidationRuleRegistry {

    private final Map<String, CustomValidationRule> rulesByName;

    public CustomValidationRuleRegistry(List<CustomValidationRule> customRules) {
        Map<String, CustomValidationRule> map = new LinkedHashMap<>();
        for (CustomValidationRule rule : customRules) {
            String name = normalize(rule.name());
            map.putIfAbsent(name, rule);
        }
        this.rulesByName = Map.copyOf(map);
    }

    public CustomValidationRule get(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return rulesByName.get(normalize(name));
    }

    private static String normalize(String name) {
        return name == null ? null : name.trim().toLowerCase(Locale.ROOT);
    }
}
