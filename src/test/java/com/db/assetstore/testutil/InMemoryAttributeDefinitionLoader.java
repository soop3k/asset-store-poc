package com.db.assetstore.testutil;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.service.type.AttributeDefinition;
import com.db.assetstore.domain.service.type.AttributeDefinitionLoader;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.db.assetstore.domain.service.type.ConstraintDefinition;
import com.db.assetstore.domain.service.type.DefaultAttributeDefinitionRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;

public final class InMemoryAttributeDefinitionLoader implements AttributeDefinitionLoader {

    private final Map<AssetType, AttributeDefinitions> definitions;

    private InMemoryAttributeDefinitionLoader(Map<AssetType, AttributeDefinitions> definitions) {
        this.definitions = definitions;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public AttributeDefinitions load(AssetType type) {
        return definitions.getOrDefault(type, AttributeDefinitions.empty());
    }

    public AttributeDefinitionRegistry toRegistry() {
        return new DefaultAttributeDefinitionRegistry(List.of(this));
    }

    public static final class Builder {
        private final Map<AssetType, Map<String, AttributeDefinition>> definitions = new HashMap<>();
        private final Map<AssetType, Map<String, List<ConstraintDefinition>>> constraints = new HashMap<>();

        private Builder() {
        }

        public Builder withAttribute(AttributeDefinition definition, List<ConstraintDefinition> rules) {
            AssetType type = definition.assetType();
            definitions.computeIfAbsent(type, ignored -> new HashMap<>()).put(definition.name(), definition);
            constraints.computeIfAbsent(type, ignored -> new HashMap<>()).put(definition.name(), new ArrayList<>(rules));
            return this;
        }

        public Builder withAttribute(AttributeDefinition definition, ConstraintDefinition... rules) {
            return withAttribute(definition, List.of(rules));
        }

        public Builder withAttributes(AssetType type,
                                       Map<String, AttributeDefinition> definitions,
                                       Map<String, List<ConstraintDefinition>> rules) {
            this.definitions.computeIfAbsent(type, ignored -> new HashMap<>()).putAll(definitions);
            Map<String, List<ConstraintDefinition>> typeConstraints =
                    this.constraints.computeIfAbsent(type, ignored -> new HashMap<>());
            rules.forEach((name, constraintList) ->
                    typeConstraints.put(name, new ArrayList<>(constraintList)));
            return this;
        }

        public Builder withAssetType(AssetType type) {
            definitions.computeIfAbsent(type, ignored -> new HashMap<>());
            constraints.computeIfAbsent(type, ignored -> new HashMap<>());
            return this;
        }


        public InMemoryAttributeDefinitionLoader build() {
            Map<AssetType, AttributeDefinitions> prepared = new HashMap<>();
            Set<AssetType> assetTypes = new HashSet<>();
            assetTypes.addAll(definitions.keySet());
            assetTypes.addAll(constraints.keySet());

            for (AssetType type : assetTypes) {
                Map<String, AttributeDefinition> typeDefinitions = definitions.getOrDefault(type, emptyMap());
                Map<String, List<ConstraintDefinition>> typeConstraints = constraints.getOrDefault(type, emptyMap());

                Map<String, AttributeDefinition> immutableDefinitions = Map.copyOf(new HashMap<>(typeDefinitions));
                Map<String, List<ConstraintDefinition>> immutableConstraints = typeConstraints.entrySet().stream()
                        .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey,
                                entry -> List.copyOf(entry.getValue())));

                prepared.put(type, new AttributeDefinitions(immutableDefinitions, immutableConstraints));
            }

            return new InMemoryAttributeDefinitionLoader(Map.copyOf(prepared));
        }

        public AttributeDefinitionRegistry buildRegistry() {
            return build().toRegistry();
        }
    }
}
