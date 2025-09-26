package com.db.assetstore.domain.service.validation.rule;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.domain.service.type.AttributeDefinition;
import com.db.assetstore.domain.service.type.ConstraintDefinition;

import java.util.List;
import java.util.Objects;

public final class AttributeValidationContext {

    private final AssetType assetType;
    private final AttributeDefinition definition;
    private final AttributesCollection attributes;
    private final ConstraintDefinition constraint;

    public AttributeValidationContext(AssetType assetType,
                                      AttributeDefinition definition,
                                      AttributesCollection attributes,
                                      ConstraintDefinition constraint) {
        this.assetType = Objects.requireNonNull(assetType, "assetType");
        this.definition = Objects.requireNonNull(definition, "definition");
        this.attributes = attributes == null ? AttributesCollection.empty() : attributes;
        this.constraint = constraint;
    }

    public AssetType assetType() {
        return assetType;
    }

    public AttributeDefinition definition() {
        return definition;
    }

    public List<AttributeValue<?>> values() {
        return attributes.getAll(definition.name());
    }

    public AttributesCollection attributes() {
        return attributes;
    }

    public ConstraintDefinition constraint() {
        return constraint;
    }

    public AttributeValidationContext withConstraint(ConstraintDefinition newConstraint) {
        return new AttributeValidationContext(assetType, definition, attributes, newConstraint);
    }
}
