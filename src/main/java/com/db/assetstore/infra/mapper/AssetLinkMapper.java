package com.db.assetstore.infra.mapper;

import com.db.assetstore.domain.model.link.AssetLink;
import com.db.assetstore.infra.jpa.link.AssetLinkEntity;
import org.mapstruct.Mapper;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * MapStruct-backed mapper for converting asset link JPA entities into domain models.
 */
@Mapper(componentModel = "spring")
public interface AssetLinkMapper {

    AssetLink toModel(AssetLinkEntity entity);

    default List<AssetLink> toModels(Collection<AssetLinkEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }
        return entities.stream()
                .filter(Objects::nonNull)
                .map(this::toModel)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}

