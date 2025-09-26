package com.db.assetstore.domain.service.type;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.type.AttributeType;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Immutable description of an attribute definition used across the domain layer.
 */
public record AttributeDefinition(AssetType assetType,
                                  String name,
                                  AttributeType attributeType) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public AttributeDefinition {
        assetType = Objects.requireNonNull(assetType, "assetType");
        name = Objects.requireNonNull(name, "name").trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Attribute name must not be blank");
        }
        attributeType = attributeType == null ? AttributeType.STRING : attributeType;
    }
}
