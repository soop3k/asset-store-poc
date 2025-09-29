package com.db.assetstore.infra.mapper;

import com.db.assetstore.domain.service.type.AttributeDefinition;
import com.db.assetstore.domain.service.type.ConstraintDefinition;
import com.db.assetstore.infra.jpa.AttributeDefEntity;
import com.db.assetstore.infra.jpa.ConstraintDefEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AttributeDefinitionMapper {

    @Mapping(target = "assetType", source = "type")
    @Mapping(target = "attributeType", source = "valueType")
    AttributeDefinition toModel(AttributeDefEntity entity);

    @Mapping(target = "attribute", source = "attribute")
    @Mapping(target = "rule", source = "entity.rule")
    @Mapping(target = "value", source = "entity.value")
    ConstraintDefinition toModelConstraint(AttributeDefinition attribute, ConstraintDefEntity entity);

    default List<ConstraintDefinition> toModelConstraints(AttributeDefinition attribute,
                                                          List<ConstraintDefEntity> entities) {
        return entities.stream()
                .map(entity -> toModelConstraint(attribute, entity))
                .toList();
    }

}
