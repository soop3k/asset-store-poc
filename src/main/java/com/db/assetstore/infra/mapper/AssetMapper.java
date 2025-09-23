package com.db.assetstore.infra.mapper;

import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.domain.model.Asset;

import lombok.RequiredArgsConstructor;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Mapper(componentModel = "spring", uses = {AttributeMapper.class, AttributesCollectionMapper.class})
public abstract class AssetMapper {

    @Autowired
    private AttributesCollectionMapper mapper;

    @Mappings({
        @Mapping(target = "softDelete", expression = "java(entity.getDeleted() != 0)"),
        @Mapping(target = "attributes", source = "attributes")
    })
    public abstract Asset toModel(AssetEntity entity);

    @Mappings({
        @Mapping(target = "deleted", expression = "java(asset.isSoftDelete() ? 1 : 0)"),
        @Mapping(target = "attributes", ignore = true)
    })
    public abstract AssetEntity toEntity(Asset asset);

    public abstract List<Asset> toModelList(List<AssetEntity> entities);
    public abstract List<AssetEntity> toEntityList(List<Asset> models);

    @AfterMapping
    void mapAttributes(Asset src, @MappingTarget AssetEntity assetEntity) {
        var attrEntities = mapper.toEntities(src.getAttributes(), assetEntity);
        assetEntity.setAttributes(attrEntities);
    }
}

