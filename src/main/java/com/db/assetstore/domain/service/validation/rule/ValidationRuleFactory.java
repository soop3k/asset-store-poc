package com.db.assetstore.domain.service.validation.rule;

import com.db.assetstore.domain.service.type.AttributeDefinition;
import com.db.assetstore.domain.service.type.ConstraintDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ValidationRuleFactory {

    private final CustomValidationRuleRegistry customRegistry;

    public List<ValidationRule> build(AttributeDefinition definition,
                                      List<ConstraintDefinition> constraints) {

        if (constraints.isEmpty()) {
            return List.of();
        }

        var rules = new ArrayList<ValidationRule>();
        for (var constraint : constraints) {
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
        return rules;
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
