package com.db.assetstore.infra.mapper;

import com.db.assetstore.domain.model.link.AssetLink;
import com.db.assetstore.infra.jpa.AssetLinkEntity;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface AssetLinkMapper {
    AssetLink toModel(AssetLinkEntity entity);
    List<AssetLink> toModelList(List<AssetLinkEntity> entities);
}
