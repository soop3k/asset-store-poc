package com.db.assetstore.domain.service.type;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.type.AttributeType;
import lombok.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Immutable description of an attribute definition used across the domain layer.
 */
public final class AttributeDefinition implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final AssetType assetType;
    private final String name;
    private final AttributeType attributeType;
    private final boolean required;

    public AttributeDefinition(@NonNull AssetType assetType,
                               @NonNull String name,
                               AttributeType attributeType,
                               boolean required) {
        this.assetType = assetType;
        var normalizedName = name.trim();
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Attribute name must not be blank");
        }
        this.name = normalizedName;
        this.attributeType = attributeType == null ? AttributeType.STRING : attributeType;
        this.required = required;
    }

    public AssetType assetType() {
        return assetType;
    }

    public String name() {
        return name;
    }

    public AttributeType attributeType() {
        return attributeType;
    }

    public boolean required() {
        return required;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AttributeDefinition that)) {
            return false;
        }
        return assetType == that.assetType
                && required == that.required
                && Objects.equals(name, that.name)
                && attributeType == that.attributeType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetType, name, attributeType, required);
    }

    @Override
    public String toString() {
        return "AttributeDefinition{" +
                "assetType=" + assetType +
                ", name='" + name + '\'' +
                ", attributeType=" + attributeType +
                ", required=" + required +
                '}';
    }
}
