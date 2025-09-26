package com.db.assetstore.domain.service.type;

import lombok.NonNull;

import java.io.Serial;
import java.io.Serializable;

/**
 * Describes a validation constraint associated with an attribute. The {@code rule} names the
 * validation rule that should be executed while {@code value} carries rule specific configuration
 * such as numeric bounds or allowed values.
 */
public record ConstraintDefinition(@NonNull AttributeDefinition attribute,
                                   @NonNull Rule rule,
                                   String value) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public ConstraintDefinition {
        value = value == null || value.isBlank() ? null : value.trim();
    }

    public enum Rule {
        TYPE,
        REQUIRED,
        MIN_MAX,
        ENUM,
        LENGTH,
        CUSTOM;

        public static Rule from(@NonNull String name) {
            String normalized = name.trim();
            if (normalized.isEmpty()) {
                throw new IllegalArgumentException("Rule name must not be blank");
            }
            try {
                return Rule.valueOf(normalized.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Unsupported rule: " + name, ex);
            }
        }
    }
}
