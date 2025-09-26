package com.db.assetstore.domain.service.validation.rule;

import com.db.assetstore.domain.service.type.AttributeDefinition;
import com.db.assetstore.domain.service.type.ConstraintDefinition;
import com.db.assetstore.util.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ValidationRuleFactory {

    private final CustomValidationRuleRegistry customRegistry;

    public ValidationRuleFactory(CustomValidationRuleRegistry customRegistry) {
        this.customRegistry = customRegistry;
    }

    public List<ValidationRule> build(AttributeDefinition definition,
                                      List<ConstraintDefinition> constraints) {
        var rules = new ArrayList<ValidationRule>();
        var safeConstraints = CollectionUtils.<List<ConstraintDefinition>>emptyIfNullOrEmpty(constraints);
        if (safeConstraints.isEmpty()) {
            return List.copyOf(rules);
        }
        for (var constraint : safeConstraints) {
            if (constraint == null) {
                continue;
            }
            switch (constraint.rule()) {
                case TYPE -> rules.add(new TypeRule(definition));
                case REQUIRED -> rules.add(new RequiredRule(definition.name()));
                case MIN_MAX -> rules.add(new MinMaxRule(definition.name(), constraint.value()));
                case ENUM -> rules.add(new EnumRule(definition.name(), constraint.value()));
                case LENGTH -> rules.add(new LengthRule(definition.name(), constraint.value()));
                case CUSTOM -> rules.add(resolveCustom(constraint));
                default -> throw new AttributeValidationException("Unsupported rule: " + constraint.rule());
            }
        }
        return List.copyOf(rules);
    }

    private ValidationRule resolveCustom(ConstraintDefinition constraint) {
        var name = constraint.value();
        var delegate = customRegistry.get(name);
        if (delegate == null) {
            throw new AttributeValidationException("Custom validation rule not registered: " + name);
        }
        return new CustomRule(delegate);
    }
}
