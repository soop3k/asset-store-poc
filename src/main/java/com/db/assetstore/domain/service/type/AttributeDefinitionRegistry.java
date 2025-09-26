package com.db.assetstore.domain.service.type;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.type.AttributeType;

import java.util.Map;


public interface AttributeDefinitionRegistry {

    Map<String, AttributeDefinition> getDefinitions(AssetType type);

    void refresh();

    record AttributeDefinition(String name, AttributeType attributeType, boolean required) {
    }
}

