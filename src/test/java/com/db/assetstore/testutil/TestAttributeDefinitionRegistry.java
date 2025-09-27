package com.db.assetstore.testutil;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.service.type.AttributeDefinition;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.db.assetstore.domain.service.type.ConstraintDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

public final class TestAttributeDefinitionRegistry implements AttributeDefinitionRegistry {

    private final Map<AssetType, Map<String, AttributeDefinition>> definitions;
    private final Map<AssetType, Map<String, List<ConstraintDefinition>>> constraints;

    private TestAttributeDefinitionRegistry(Builder builder) {
        this.definitions = toUnmodifiable(builder.definitions);
        this.constraints = toUnmodifiable(builder.constraints);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Map<String, AttributeDefinition> getDefinitions(AssetType type) {
        return definitions.getOrDefault(type, emptyMap());
    }

    @Override
    public Map<String, List<ConstraintDefinition>> getConstraints(AssetType type) {
        return constraints.getOrDefault(type, emptyMap());
    }

    @Override
    public void refresh() {
        // no-op for tests
    }

    private static Map<AssetType, Map<String, AttributeDefinition>> toUnmodifiable(
            Map<AssetType, Map<String, AttributeDefinition>> source) {
        Map<AssetType, Map<String, AttributeDefinition>> copy = new HashMap<>();
        source.forEach((type, defs) -> copy.put(type, unmodifiableMap(new HashMap<>(defs))));
        return unmodifiableMap(copy);
    }

    private static Map<AssetType, Map<String, List<ConstraintDefinition>>> toUnmodifiableConstraints(
            Map<AssetType, Map<String, List<ConstraintDefinition>>> source) {
        Map<AssetType, Map<String, List<ConstraintDefinition>>> copy = new HashMap<>();
        source.forEach((type, values) -> {
            Map<String, List<ConstraintDefinition>> attrConstraints = new HashMap<>();
            values.forEach((name, constraintList) -> attrConstraints.put(name, unmodifiableList(new ArrayList<>(constraintList))));
            copy.put(type, unmodifiableMap(attrConstraints));
        });
        return unmodifiableMap(copy);
    }

    private static Map<AssetType, Map<String, List<ConstraintDefinition>>> toUnmodifiable(
            Map<AssetType, Map<String, List<ConstraintDefinition>>> source) {
        return toUnmodifiableConstraints(source);
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

        public Builder configure(Consumer<Builder> consumer) {
            consumer.accept(this);
            return this;
        }

        public TestAttributeDefinitionRegistry build() {
            return new TestAttributeDefinitionRegistry(this);
        }
    }
}
