package com.db.assetstore.infra.service.type;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.service.type.AttributeDefinition;
import com.db.assetstore.domain.service.type.AttributeDefinitionLoader;
import com.db.assetstore.domain.service.type.ConstraintDefinition;
import com.db.assetstore.infra.jpa.AttributeDefEntity;
import com.db.assetstore.infra.mapper.AttributeDefinitionMapper;
import com.db.assetstore.infra.repository.AttributeDefRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Order(1)
public class DatabaseAttributeDefinitionLoader implements AttributeDefinitionLoader {

    private final AttributeDefRepository attributeDefRepository;
    private final AttributeDefinitionMapper attributeDefinitionMapper;

    @Override
    public AttributeDefinitions load(AssetType type) {
        var definitions = new LinkedHashMap<String, AttributeDefinition>();
        var constraints = new LinkedHashMap<String, List<ConstraintDefinition>>();

        var entities = attributeDefRepository.findAllByTypeWithConstraints(type);
        for (var entity : entities) {
            var attributeDefinition = attributeDefinitionMapper.toDomain(entity);
            definitions.put(entity.getName(), attributeDefinition);

            var attributeConstraints = new ArrayList<ConstraintDefinition>();
            attributeConstraints.add(new ConstraintDefinition(attributeDefinition, ConstraintDefinition.Rule.TYPE, null));
            if (entity.isRequired()) {
                attributeConstraints.add(new ConstraintDefinition(attributeDefinition, ConstraintDefinition.Rule.REQUIRED, null));
            }
            attributeConstraints.addAll(
                    attributeDefinitionMapper.toDomainConstraints(attributeDefinition, entity.getConstraints()));
            constraints.put(entity.getName(), List.copyOf(attributeConstraints));
        }

        return new AttributeDefinitions(definitions, constraints);
    }
}
