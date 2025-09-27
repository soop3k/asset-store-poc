package com.db.assetstore.domain.service.type;

import com.db.assetstore.AssetType;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class DefaultAttributeDefinitionRegistry implements AttributeDefinitionRegistry {

    private static final CachedDefinitions EMPTY = new CachedDefinitions(Map.of(), Map.of());

    private final List<AttributeDefinitionLoader> loaders;
    private final Map<AssetType, CachedDefinitions> cache = new ConcurrentHashMap<>();

    public DefaultAttributeDefinitionRegistry(List<AttributeDefinitionLoader> loaders) {
        Objects.requireNonNull(loaders, "loaders must not be null");

        if (loaders.isEmpty()) {
            this.loaders = List.of();
            return;
        }

        var sorted = new ArrayList<>(loaders);
        AnnotationAwareOrderComparator.sort(sorted);
        this.loaders = List.copyOf(sorted);
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

    private CachedDefinitions resolve(AssetType type) {
        Objects.requireNonNull(type, "asset type must not be null");
        return cache.computeIfAbsent(type, this::buildDefinitions);
    }

    private CachedDefinitions buildDefinitions(AssetType type) {
        var preparedDefinitions = new LinkedHashMap<String, AttributeDefinition>();
        var preparedConstraints = new LinkedHashMap<String, List<ConstraintDefinition>>();

        if (loaders.isEmpty()) {
            return EMPTY;
        }

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
                preparedConstraints.put(name, List.copyOf(constraints));
            });
        }

        var immutableDefinitions = Collections.unmodifiableMap(preparedDefinitions);
        var immutableConstraints = preparedConstraints.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey,
                        entry -> List.copyOf(entry.getValue())));

        return new CachedDefinitions(immutableDefinitions, immutableConstraints);
    }

    private record CachedDefinitions(Map<String, AttributeDefinition> definitions,
                                     Map<String, List<ConstraintDefinition>> constraints) {

        private CachedDefinitions {
            Objects.requireNonNull(definitions, "definitions must not be null");
            Objects.requireNonNull(constraints, "constraints must not be null");
        }
    }
}
