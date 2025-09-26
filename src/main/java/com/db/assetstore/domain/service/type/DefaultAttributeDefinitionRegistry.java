package com.db.assetstore.domain.service.type;

import com.db.assetstore.AssetType;
import com.db.assetstore.util.CollectionUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class DefaultAttributeDefinitionRegistry implements AttributeDefinitionRegistry {

    private static final CachedDefinitions EMPTY = new CachedDefinitions(Map.of(), Map.of());

    private final List<AttributeDefinitionLoader> loaders;
    private final Map<AssetType, CachedDefinitions> cache = new ConcurrentHashMap<>();

    public DefaultAttributeDefinitionRegistry(List<AttributeDefinitionLoader> loaders) {
        var safeLoaders = CollectionUtils.<List<AttributeDefinitionLoader>>emptyIfNullOrEmpty(loaders);
        if (safeLoaders.isEmpty()) {
            this.loaders = List.of();
            return;
        }

        var sorted = new ArrayList<>(safeLoaders);
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
        if (type == null) {
            return EMPTY;
        }
        return cache.computeIfAbsent(type, this::buildDefinitions);
    }

    private CachedDefinitions buildDefinitions(AssetType type) {
        Map<String, AttributeDefinition> preparedDefinitions = new LinkedHashMap<>();
        Map<String, List<ConstraintDefinition>> preparedConstraints = new LinkedHashMap<>();

        if (loaders.isEmpty()) {
            return EMPTY;
        }

        for (var loader : loaders) {
            var loaded = loader.load(type);

            loaded.definitions().forEach((name, definition) -> {
                var constraints = new ArrayList<>(
                        CollectionUtils.<List<ConstraintDefinition>>emptyIfNullOrEmpty(
                                loaded.constraints().get(name)));

                CollectionUtils.<List<ConstraintDefinition>>emptyIfNullOrEmpty(
                        preparedConstraints.get(name))
                        .stream()
                        .filter(constraint -> constraint.rule() == ConstraintDefinition.Rule.CUSTOM)
                        .forEach(constraint -> constraints.add(
                                new ConstraintDefinition(definition, constraint.rule(), constraint.value())));

                preparedDefinitions.put(name, definition);
                preparedConstraints.put(name, List.copyOf(constraints));
            });
        }

        Map<String, AttributeDefinition> immutableDefinitions = Collections.unmodifiableMap(preparedDefinitions);
        Map<String, List<ConstraintDefinition>> immutableConstraints = preparedConstraints.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey,
                        entry -> List.copyOf(entry.getValue())));

        return new CachedDefinitions(immutableDefinitions, immutableConstraints);
    }

    private record CachedDefinitions(Map<String, AttributeDefinition> definitions,
                                     Map<String, List<ConstraintDefinition>> constraints) {

        private CachedDefinitions {
            definitions = CollectionUtils.<Map<String, AttributeDefinition>>emptyIfNullOrEmpty(definitions);
            constraints = CollectionUtils.<Map<String, List<ConstraintDefinition>>>emptyIfNullOrEmpty(constraints);
        }
    }
}
