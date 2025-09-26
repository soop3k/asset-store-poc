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

    public void validate(AssetType type, AttributesCollection attributes) {
        Map<String, AttributeDefinition> definitionMap = attributeDefinitionRegistry.safeDefinitions(type);
        Map<String, List<ConstraintDefinition>> constraintMap = attributeDefinitionRegistry.safeConstraints(type);

        AttributesCollection provided = attributes == null ? AttributesCollection.empty() : attributes;
        Map<String, List<AttributeValue<?>>> values = provided.asMapView();
        for (String attributeName : values.keySet()) {
            if (!definitionMap.containsKey(attributeName)) {
                throw new RuleViolationException("UNKNOWN_ATTRIBUTE", attributeName,
                        "Unknown attribute definition: " + attributeName);
            }
        }

        for (AttributeDefinition definition : definitionMap.values()) {
            AttributeValidationContext baseContext = new AttributeValidationContext(
                    type,
                    definition,
                    provided,
                    null
            );

            List<ConstraintDefinition> constraints = constraintMap.getOrDefault(definition.name(), List.of());
            for (ConstraintDefinition constraint : constraints) {
                ValidationRule rule = validationRuleRegistry.get(constraint.rule());
                if (rule == null) {
                    throw new AttributeValidationException(
                            "No validation rule registered for " + constraint.rule());
                }
                rule.validate(baseContext.withConstraint(constraint));
            }
        }
    }
}
