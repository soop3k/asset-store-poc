package com.db.assetstore.domain.service.type;

import lombok.NonNull;

import java.io.Serial;
import java.io.Serializable;


public record ConstraintDefinition(@NonNull AttributeDefinition attribute,
                                   @NonNull Rule rule,
                                   String value) {

    public enum Rule {
        TYPE,
        REQUIRED,
        MIN_MAX,
        ENUM,
        LENGTH,
        CUSTOM;

        public static Rule from(@NonNull String name) {
            try {
                return Rule.valueOf(name.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Unsupported rule: " + name, ex);
            }
        }
    }
}
