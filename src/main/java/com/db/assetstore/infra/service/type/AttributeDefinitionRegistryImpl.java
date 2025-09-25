package com.db.assetstore.infra.service.type;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry.AttributeDefinition;
import com.db.assetstore.infra.jpa.AttributeDefEntity;
import com.db.assetstore.infra.repository.AttributeDefRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttributeDefinitionRegistryImpl implements AttributeDefinitionRegistry {

    private final SchemaAttributeDefinitionLoader schemaLoader;
    private final AttributeDefRepository attributeDefRepository;

    private final Map<AssetType, Map<String, AttributeDefinition>> cache = new ConcurrentHashMap<>();

    @Override
    public Map<String, AttributeDefinition> getDefinitions(AssetType type) {
        if (type == null) {
            return Collections.emptyMap();
        }
        return cache.computeIfAbsent(type, this::loadDefinitions);
    }

    @Override
    public void refresh() {
        cache.clear();
    }

    private Map<String, AttributeDefinition> loadDefinitions(AssetType type) {
        Optional<Map<String, AttributeDefinition>> schemaDefinitions = schemaLoader.load(type);
        Map<String, AttributeDefinition> dbDefinitions = loadFromDatabase(type);

        if (schemaDefinitions.isEmpty()) {
            return dbDefinitions.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(dbDefinitions);
        }

        Map<String, AttributeDefinition> merged = new LinkedHashMap<>(schemaDefinitions.get());
        dbDefinitions.forEach(merged::putIfAbsent);

        log.debug("Loaded {} attribute definitions for {} (schema={}, db={})",
                merged.size(), type, schemaDefinitions.get().size(), dbDefinitions.size());

        return Collections.unmodifiableMap(merged);
    }

    private Map<String, AttributeDefinition> loadFromDatabase(AssetType type) {
        List<AttributeDefEntity> entities = attributeDefRepository.findAllByType(type);
        if (entities.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, AttributeDefinition> definitions = new LinkedHashMap<>();
        for (AttributeDefEntity entity : entities) {
            AttributeDefinition definition = new AttributeDefinition(
                    entity.getName(),
                    entity.getValueType(),
                    entity.isRequired()
            );
            definitions.put(entity.getName(), definition);
        }
        return definitions;
    }
}

