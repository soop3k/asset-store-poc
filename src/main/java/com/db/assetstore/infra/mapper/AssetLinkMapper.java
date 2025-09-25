package com.db.assetstore.infra.mapper;

import com.db.assetstore.domain.model.link.AssetLink;
import com.db.assetstore.infra.jpa.AssetLinkEntity;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface AssetLinkMapper {
    @Mapping(target = "deactivatedAt", expression = "java(java.util.Optional.ofNullable(entity.getDeactivatedAt()))")
    AssetLink toModel(AssetLinkEntity entity);
    List<AssetLink> toModelList(List<AssetLinkEntity> entities);
}
