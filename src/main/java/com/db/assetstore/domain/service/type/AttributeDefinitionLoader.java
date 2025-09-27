package com.db.assetstore.domain.service.type;

import com.db.assetstore.AssetType;

import java.util.List;
import java.util.Map;

public interface AttributeDefinitionLoader {

    AttributeDefinitions load(AssetType type);

    record AttributeDefinitions(Map<String, AttributeDefinition> definitions,
                                Map<String, List<ConstraintDefinition>> constraints) {

        public static AttributeDefinitions empty() {
            return new AttributeDefinitions(Map.of(), Map.of());
        }
    }
}
