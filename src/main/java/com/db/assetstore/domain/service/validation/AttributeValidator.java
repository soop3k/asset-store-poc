package com.db.assetstore.domain.service.validation;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.domain.service.type.AttributeDefinition;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.db.assetstore.domain.service.type.ConstraintDefinition;
import com.db.assetstore.domain.service.validation.rule.AttributeValidationContext;
import com.db.assetstore.domain.service.validation.rule.AttributeValidationException;
import com.db.assetstore.domain.service.validation.rule.RuleViolationException;
import com.db.assetstore.domain.service.validation.rule.ValidationRule;
import com.db.assetstore.domain.service.validation.rule.ValidationRuleFactory;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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
                         @NonNull AttributesCollection attributes,
                         @NonNull ValidationMode mode) {
        validateInternal(type, attributes, mode);
    }

    public void validate(AssetType type, @NonNull AttributesCollection attributes) {
        validate(type, attributes, ValidationMode.FULL);
    }

    private void validateInternal(AssetType type,
                                  AttributesCollection attributes,
                                  ValidationMode mode) {
        var definitionMap = attributeDefinitionRegistry.getDefinitions(type);
        var constraintMap = attributeDefinitionRegistry.getConstraints(type);

        var values = attributes.asMapView();
        enforceKnownAttributes(mode, definitionMap, values);

        for (var definition : definitionMap.values()) {
            var providedValues = values.get(definition.name());
            var attributeProvided = providedValues != null && !providedValues.isEmpty();

            var context = new AttributeValidationContext(type, definition, attributes);

            var constraints = constraintMap.getOrDefault(definition.name(), List.of());
            var rules = validationRuleFactory.build(definition, constraints);
            for (var rule : rules) {
                if (!mode.enforceRequiredForMissing()
                        && rule.rule() == ConstraintDefinition.Rule.REQUIRED
                        && !attributeProvided) {
                    continue;
                }

                rule.validate(context);
            }
        }
    }

    private void enforceKnownAttributes(ValidationMode mode,
                                        Map<String, AttributeDefinition> definitionMap,
                                        Map<String, List<AttributeValue<?>>> values) {
        if (!mode.failOnUnknownAttributes()) {
            return;
        }

        for (var attributeName : values.keySet()) {
            if (!definitionMap.containsKey(attributeName)) {
                throw new RuleViolationException("UNKNOWN_ATTRIBUTE", attributeName,
                        "Unknown attribute definition: " + attributeName);
            }
        }
    }
}
