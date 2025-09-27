package com.db.assetstore.domain.service.validation;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.domain.service.type.AttributeDefinition;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.db.assetstore.domain.service.validation.rule.AttributeValidationContext;
import com.db.assetstore.domain.service.validation.rule.AttributeValidationErrorsException;
import com.db.assetstore.domain.service.validation.rule.RuleViolation;
import com.db.assetstore.domain.service.validation.rule.RuleViolationException;
import com.db.assetstore.domain.service.validation.rule.ValidationRule;
import com.db.assetstore.domain.service.validation.rule.ValidationRuleFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class AttributeValidator {

    private final AttributeDefinitionRegistry attributeDefinitionRegistry;
    private final ValidationRuleFactory validationRuleFactory;

    public AttributeValidator(AttributeDefinitionRegistry attributeDefinitionRegistry,
                              ValidationRuleFactory validationRuleFactory) {
        this.attributeDefinitionRegistry = attributeDefinitionRegistry;
        this.validationRuleFactory = validationRuleFactory;
    }

    public void validate(AssetType type,
                         AttributesCollection attributes,
                         ValidationMode mode) {
        validateInternal(type, attributes, mode);
    }

    public void validate(AssetType type, AttributesCollection attributes) {
        validate(type, attributes, ValidationMode.FULL);
    }

    private void validateInternal(AssetType type,
                                  AttributesCollection attributes,
                                  ValidationMode mode) {
        var definitionMap = attributeDefinitionRegistry.getDefinitions(type);
        var constraintMap = attributeDefinitionRegistry.getConstraints(type);

        var values = attributes.asMapView();
        var violations = new ArrayList<RuleViolation>();

        enforceKnownAttributes(mode, definitionMap, values, violations);

        for (var definition : definitionMap.values()) {
            var context = new AttributeValidationContext(type, definition, attributes);

            var constraints = constraintMap.getOrDefault(definition.name(), List.of());
            var rules = validationRuleFactory.build(definition, constraints);
            for (var rule : rules) {
                if (rule.shouldValidate(context, mode)) {
                    try {
                        rule.validate(context);
                    } catch (RuleViolationException ex) {
                        violations.add(ex.violation());
                    }
                }
            }
        }

        if (!violations.isEmpty()) {
            throw new AttributeValidationErrorsException(violations);
        }
    }

    private void enforceKnownAttributes(ValidationMode mode,
                                        Map<String, AttributeDefinition> definitionMap,
                                        Map<String, List<AttributeValue<?>>> values,
                                        List<RuleViolation> violations) {
        if (!mode.failOnUnknownAttributes()) {
            return;
        }

        for (var attributeName : values.keySet()) {
            if (!definitionMap.containsKey(attributeName)) {
                violations.add(RuleViolation.forAttribute("UNKNOWN_ATTRIBUTE", attributeName,
                        "Unknown attribute definition", definitionMap.keySet(), attributeName));
            }
        }
    }
}
