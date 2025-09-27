package com.db.assetstore.domain.service.type;

import com.db.assetstore.AssetType;

import java.util.List;
import java.util.Map;

public interface AttributeDefinitionRegistry {

    Map<String, AttributeDefinition> getDefinitions(AssetType type);

    Map<String, List<ConstraintDefinition>> getConstraints(AssetType type);

    default List<ConstraintDefinition> getConstraints(AssetType type, String attributeName) {
        return getConstraints(type).getOrDefault(attributeName, List.of());
    }

    default AttributeDefinition getDefinition(AssetType type, String attributeName) {
        return getDefinitions(type).get(attributeName);
    }

    void refresh();
}
