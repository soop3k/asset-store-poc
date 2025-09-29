package com.db.assetstore.domain.service.validation.rule;

import com.db.assetstore.domain.model.asset.AssetType;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.domain.service.type.AttributeDefinition;
import lombok.NonNull;

import java.util.List;

public final class AttributeValidationContext {

    private final AssetType assetType;
    private final AttributeDefinition definition;
    private final AttributesCollection attributes;
    public AttributeValidationContext(@NonNull AssetType assetType,
                                      @NonNull AttributeDefinition definition,
                                      @NonNull AttributesCollection attributes) {
        this.assetType = assetType;
        this.definition = definition;
        this.attributes = attributes;
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

}
