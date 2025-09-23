package com.db.assetstore.infra.mapper;

import com.db.assetstore.infra.jpa.AttributeEntity;
import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {AttributeMapper.class})
public interface AttributesCollectionMapper {

    default AttributesCollection fromEntities(List<AttributeEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return AttributesCollection.empty();
        }
        AttributeMapper mapper = Mappers.getMapper(AttributeMapper.class);
        List<AttributeValue<?>> values = entities.stream()
                .filter(Objects::nonNull)
                .map(mapper::toModel)
                .collect(Collectors.toList());
        return AttributesCollection.fromFlat(values);
    }

    default List<AttributeEntity> toEntities(AttributesCollection collection, AssetEntity parent) {
        if (collection == null || collection.isEmpty()) {
            return List.of();
        }
        AttributeMapper mapper = Mappers.getMapper(AttributeMapper.class);
        return collection.asListView().stream()
                .map(av -> mapper.toEntity(parent, av))
                .collect(Collectors.toList());
    }
}
