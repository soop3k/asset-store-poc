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
import com.db.assetstore.domain.service.validation.rule.ValidationRuleRegistry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AttributeValidator {

    private final AttributeDefinitionRegistry attributeDefinitionRegistry;
    private final ValidationRuleRegistry validationRuleRegistry;

    public AttributeValidator(AttributeDefinitionRegistry attributeDefinitionRegistry,
                              ValidationRuleRegistry validationRuleRegistry) {
        this.attributeDefinitionRegistry = attributeDefinitionRegistry;
        this.validationRuleRegistry = validationRuleRegistry;
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
        Map<String, AttributeDefinition> definitionMap = attributeDefinitionRegistry.safeDefinitions(type);
        Map<String, List<ConstraintDefinition>> constraintMap = attributeDefinitionRegistry.safeConstraints(type);

        AttributesCollection provided = attributes == null ? AttributesCollection.empty() : attributes;
        Map<String, List<AttributeValue<?>>> values = provided.asMapView();
        enforceKnownAttributes(mode, definitionMap, values);

        for (AttributeDefinition definition : definitionMap.values()) {
            List<AttributeValue<?>> providedValues = values.get(definition.name());
            boolean attributeProvided = providedValues != null && !providedValues.isEmpty();

            AttributeValidationContext baseContext = new AttributeValidationContext(
                    type,
                    definition,
                    provided,
                    null
            );

            List<ConstraintDefinition> constraints = constraintMap.getOrDefault(definition.name(), List.of());
            for (ConstraintDefinition constraint : constraints) {
                if (!mode.enforceRequiredForMissing()
                        && constraint.rule() == ConstraintDefinition.Rule.REQUIRED
                        && !attributeProvided) {
                    continue;
                }

                ValidationRule rule = validationRuleRegistry.get(constraint.rule());
                if (rule == null) {
                    throw new AttributeValidationException(
                            "No validation rule registered for " + constraint.rule());
                }
                rule.validate(baseContext.withConstraint(constraint));
            }
        }
    }

    private void enforceKnownAttributes(ValidationMode mode,
                                        Map<String, AttributeDefinition> definitionMap,
                                        Map<String, List<AttributeValue<?>>> values) {
        if (!mode.failOnUnknownAttributes()) {
            return;
        }

        for (String attributeName : values.keySet()) {
            if (!definitionMap.containsKey(attributeName)) {
                throw new RuleViolationException("UNKNOWN_ATTRIBUTE", attributeName,
                        "Unknown attribute definition: " + attributeName);
            }
        }
    }
}
