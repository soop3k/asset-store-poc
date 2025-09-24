package com.db.assetstore.infra.mapper;

import com.db.assetstore.domain.model.link.AssetLink;
import com.db.assetstore.infra.jpa.link.AssetLinkEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct-backed mapper for converting asset link JPA entities into domain models.
 */
@Mapper(componentModel = "spring")
public interface AssetLinkMapper {

    AssetLink toModel(AssetLinkEntity entity);

    List<AssetLink> toModels(List<AssetLinkEntity> entities);
}

