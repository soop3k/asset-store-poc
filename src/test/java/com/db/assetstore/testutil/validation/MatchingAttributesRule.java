package com.db.assetstore.testutil.validation;

import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.domain.service.type.ConstraintDefinition;
import com.db.assetstore.domain.service.validation.rule.AttributeValidationContext;
import com.db.assetstore.domain.service.validation.rule.CustomValidationRule;
import com.db.assetstore.domain.service.validation.rule.RuleViolationException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
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
        Object reference = value(attributes, context.definition().name());
        Object counterpart = value(attributes, "code");
        if (!Objects.equals(reference, counterpart)) {
            throw new RuleViolationException("CUSTOM", context.definition().name(),
                    "Attributes must match [" + context.definition().name() + ", code]");
        }
    }

    private Object value(AttributesCollection attributes, String name) {
        return attributes.get(name)
                .map(AttributeValue::value)
                .orElse(null);
    }
}
