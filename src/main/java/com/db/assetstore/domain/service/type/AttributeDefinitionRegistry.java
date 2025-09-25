package com.db.assetstore.domain.service.type;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.type.AttributeType;

import java.util.Map;

/**
 * Abstraction used by the domain to obtain attribute definitions for an asset type.
 * Infrastructure provides the actual implementation sourcing the definitions from
 * JSON schemas and/or the database.
 */
public interface AttributeDefinitionRegistry {

    /**
     * Returns attribute definitions for the given asset type. The returned map is keyed by the
     * attribute name and contains immutable definition descriptors.
     */
    Map<String, AttributeDefinition> getDefinitions(AssetType type);

    /**
     * Forces the registry to reload definitions from its underlying sources.
     */
    void refresh();

    record AttributeDefinition(String name, AttributeType attributeType, boolean required) {
    }
}

