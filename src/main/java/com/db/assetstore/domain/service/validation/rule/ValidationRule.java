package com.db.assetstore.domain.service.validation.rule;

import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.service.type.ConstraintDefinition;
import com.db.assetstore.domain.service.validation.ValidationMode;

import java.util.function.Consumer;

public interface ValidationRule {

    ConstraintDefinition.Rule rule();

    default boolean shouldValidate(AttributeValidationContext context, ValidationMode mode) {
        return true;
    }

    default void forEachPresentValue(AttributeValidationContext context,
                                     Consumer<AttributeValue<?>> consumer) {
        for (var value : context.values()) {
            if (value == null || value.value() == null) {
                continue;
            }
            consumer.accept(value);
        }
    }

    void validate(AttributeValidationContext context);
}
