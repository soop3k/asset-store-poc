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
    @Mapping(target = "required", source = "required")
    AttributeDefinition toDomain(AttributeDefEntity entity);

    @Mapping(target = "attribute", source = "attribute")
    @Mapping(target = "rule", expression = "java(com.db.assetstore.domain.service.type.ConstraintDefinition.Rule.from(entity.getName()))")
    @Mapping(target = "value", source = "entity.value")
    ConstraintDefinition toDomainConstraint(AttributeDefinition attribute, ConstraintDefEntity entity);

    default List<ConstraintDefinition> toDomainConstraints(AttributeDefinition attribute,
                                                          List<ConstraintDefEntity> entities) {
        return entities.stream()
                .map(entity -> toDomainConstraint(attribute, entity))
                .toList();
    }

}
