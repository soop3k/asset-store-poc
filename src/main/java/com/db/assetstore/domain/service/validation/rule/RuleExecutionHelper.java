package com.db.assetstore.domain.service.validation.rule;

import com.db.assetstore.domain.model.attribute.AttributeValue;

import java.util.function.Consumer;

final class RuleExecutionHelper {

    private RuleExecutionHelper() {
    }

    static void forEachPresentValue(AttributeValidationContext context,
                                    Consumer<AttributeValue<?>> consumer) {
        for (var value : context.values()) {
            if (value == null || value.value() == null) {
                continue;
            }
            consumer.accept(value);
        }
    }
}
