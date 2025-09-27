package com.db.assetstore.domain.service.validation.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;

@Component
public final class CustomValidationRuleRegistry {

    private static final Logger log = LoggerFactory.getLogger(CustomValidationRuleRegistry.class);

    private final Map<String, CustomValidationRule> rulesByName;

    public CustomValidationRuleRegistry(List<CustomValidationRule> customRules) {
        var map = new LinkedHashMap<String, CustomValidationRule>();
        for (var rule : customRules) {
            register(map, rule.name(), rule);
        }
        this.rulesByName = Map.copyOf(map);
        logLoadedRules(this.rulesByName);
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

    private void logLoadedRules(Map<String, CustomValidationRule> rules) {
        if (rules.isEmpty()) {
            log.info("Loaded 0 custom validation rules");
            return;
        }
        var descriptions = new ArrayList<String>(rules.size());
        for (var entry : rules.entrySet()) {
            descriptions.add(entry.getKey() + " -> " + entry.getValue().getClass().getName());
        }
        log.info("Loaded {} custom validation rules: {}", descriptions.size(), descriptions);
    }
}
