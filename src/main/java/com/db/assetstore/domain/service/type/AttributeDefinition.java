package com.db.assetstore.domain.service.type;

import com.db.assetstore.domain.model.asset.AssetType;
import com.db.assetstore.domain.model.type.AttributeType;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public final class AttributeDefinition {

    private final AssetType assetType;
    private final String name;
    private final AttributeType attributeType;

    public AssetType assetType() {
        return assetType;
    }

    public String name() {
        return name;
    }

    public AttributeType attributeType() {
        return attributeType;
    }

}
