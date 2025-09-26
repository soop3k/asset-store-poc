package com.db.assetstore.domain.service.validation.custom;

import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.domain.service.type.ConstraintDefinition;
import com.db.assetstore.domain.service.validation.rule.AttributeValidationContext;
import com.db.assetstore.domain.service.validation.rule.CustomValidationRule;
import com.db.assetstore.domain.service.validation.rule.RuleViolationException;

import java.util.Objects;

public class MatchingAttributesRule implements CustomValidationRule {

    @Override
    public String name() {
        return "matchingAttributes";
    }

    @Override
    public ConstraintDefinition.Rule rule() {
        return ConstraintDefinition.Rule.CUSTOM;
    }

    @Override
    public void validate(AttributeValidationContext context) {
        AttributesCollection attributes = context.attributes();
        Object reference = firstValue(attributes, context.definition().name());
        Object counterpart = firstValue(attributes, "code");
        if (!Objects.equals(reference, counterpart)) {
            throw new RuleViolationException("CUSTOM", context.definition().name(),
                    "Attributes must match [" + context.definition().name() + ", code]");
        }
    }

    private Object firstValue(AttributesCollection attributes, String name) {
        return attributes.getFirst(name)
                .map(value -> value.value())
                .orElse(null);
    }
}
