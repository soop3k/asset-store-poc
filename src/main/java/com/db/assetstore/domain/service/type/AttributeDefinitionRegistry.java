package com.db.assetstore.domain.service.type;

import com.db.assetstore.AssetType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface AttributeDefinitionRegistry {

    Map<String, AttributeDefinition> getDefinitions(AssetType type);

    Map<String, List<ConstraintDefinition>> getConstraints(AssetType type);

    default List<ConstraintDefinition> getConstraints(AssetType type, String attributeName) {
        Map<String, List<ConstraintDefinition>> constraints = getConstraints(type);
        if (constraints == null) {
            return List.of();
        }
        return constraints.getOrDefault(attributeName, List.of());
    }

    default AttributeDefinition getDefinition(AssetType type, String attributeName) {
        Map<String, AttributeDefinition> definitions = getDefinitions(type);
        if (definitions == null) {
            return null;
        }
        return definitions.get(attributeName);
    }

    default Map<String, AttributeDefinition> safeDefinitions(AssetType type) {
        Map<String, AttributeDefinition> definitions = getDefinitions(type);
        return definitions == null ? Collections.emptyMap() : definitions;
    }

    default Map<String, List<ConstraintDefinition>> safeConstraints(AssetType type) {
        Map<String, List<ConstraintDefinition>> constraints = getConstraints(type);
        return constraints == null ? Collections.emptyMap() : constraints;
    }

    void refresh();
}
