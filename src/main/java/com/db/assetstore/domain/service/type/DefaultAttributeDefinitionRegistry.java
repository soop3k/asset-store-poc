package com.db.assetstore.domain.service.type;

import com.db.assetstore.domain.model.asset.AssetType;
import lombok.NonNull;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DefaultAttributeDefinitionRegistry implements AttributeDefinitionRegistry {

    private final List<AttributeDefinitionLoader> loaders;
    private final Map<AssetType, CachedDefinitions> cache = new ConcurrentHashMap<>();

    public DefaultAttributeDefinitionRegistry(@NonNull List<AttributeDefinitionLoader> loaders) {
        AnnotationAwareOrderComparator.sort(loaders);
        this.loaders = loaders;
    }

    @Override
    public Map<String, AttributeDefinition> getDefinitions(AssetType type) {
        return resolve(type).definitions();
    }

    @Override
    public Map<String, List<ConstraintDefinition>> getConstraints(AssetType type) {
        return resolve(type).constraints();
    }

    @Override
    public void refresh() {
        cache.clear();
    }

    private CachedDefinitions resolve(@NonNull AssetType type) {
        return cache.computeIfAbsent(type, this::buildDefinitions);
    }

    private CachedDefinitions buildDefinitions(AssetType type) {
        var preparedDefinitions = new LinkedHashMap<String, AttributeDefinition>();
        var preparedConstraints = new LinkedHashMap<String, List<ConstraintDefinition>>();

        for (var loader : loaders) {
            var loaded = loader.load(type);

            loaded.definitions().forEach((name, definition) -> {
                var loadedConstraints = loaded.constraints().get(name);
                var constraints = loadedConstraints == null
                        ? new ArrayList<ConstraintDefinition>()
                        : new ArrayList<>(loadedConstraints);

                var existingConstraints = preparedConstraints.get(name);
                if (existingConstraints != null) {
                    existingConstraints.stream()
                        .filter(constraint -> constraint.rule() == ConstraintDefinition.Rule.CUSTOM)
                        .forEach(constraint -> constraints.add(
                                new ConstraintDefinition(definition, constraint.rule(), constraint.value())));
                }

                preparedDefinitions.put(name, definition);
                preparedConstraints.put(name, constraints);
            });
        }

        return new CachedDefinitions(preparedDefinitions, preparedConstraints);
    }

    private record CachedDefinitions(
            Map<String, AttributeDefinition> definitions,
            Map<String, List<ConstraintDefinition>> constraints) {
    }
}
