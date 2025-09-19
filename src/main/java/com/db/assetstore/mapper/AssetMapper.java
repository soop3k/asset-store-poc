package com.db.assetstore.mapper;

import com.db.assetstore.jpa.AssetEntity;
import com.db.assetstore.model.Asset;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AssetMapper {
    AssetMapper INSTANCE = Mappers.getMapper(AssetMapper.class);

    @Mappings({
            @Mapping(target = "deleted", expression = "java(asset.isSoftDelete() ? 1 : 0)"),
            @Mapping(target = "attributes", ignore = true)
    })
    AssetEntity toEntity(Asset asset);

    @Mappings({
            @Mapping(target = "softDelete", expression = "java(entity.getDeleted() != 0)"),
            @Mapping(target = "attributes", ignore = true)
    })
    void updateModelFromEntity(AssetEntity entity, @MappingTarget Asset target);
}
