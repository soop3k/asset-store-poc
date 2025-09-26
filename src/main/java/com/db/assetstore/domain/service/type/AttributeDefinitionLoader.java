package com.db.assetstore.domain.service.type;

import com.db.assetstore.AssetType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Loads attribute definitions and their constraints for a given asset type. Implementations
 * typically read a particular source (database, schema files, etc.) and are orchestrated by the
 * domain registry which decides how to combine them.
 */
public interface AttributeDefinitionLoader {

    AttributeDefinitions load(AssetType type);

    record AttributeDefinitions(Map<String, AttributeDefinition> definitions,
                                Map<String, List<ConstraintDefinition>> constraints) {

        public AttributeDefinitions {
            definitions = definitions == null ? Map.of() : Map.copyOf(definitions);
            constraints = constraints == null ? Map.of() : constraints.entrySet().stream()
                    .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey,
                            entry -> List.copyOf(entry.getValue())));
        }

        public static AttributeDefinitions empty() {
            return new AttributeDefinitions(Map.of(), Map.of());
        }
    }
}
