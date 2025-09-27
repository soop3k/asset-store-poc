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
        var map = new LinkedHashMap<String, CustomValidationRule>();
        for (var rule : customRules) {
            register(map, rule.name(), rule);
            register(map, rule.getClass().getSimpleName(), rule);
            register(map, rule.getClass().getName(), rule);
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

    private static void register(Map<String, CustomValidationRule> target,
                                 String key,
                                 CustomValidationRule rule) {
        if (key == null || key.isBlank()) {
            return;
        }
        target.putIfAbsent(normalize(key), rule);
    }
}
