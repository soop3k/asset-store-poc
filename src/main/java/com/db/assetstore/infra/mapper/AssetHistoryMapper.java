package com.db.assetstore.infra.mapper;

import com.db.assetstore.domain.model.asset.AssetHistory;
import com.db.assetstore.infra.jpa.AssetHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AssetHistoryMapper {

    @Mapping(target = "assetId", expression = "java(entity.getAsset() != null ? entity.getAsset().getId() : null)")
    @Mapping(target = "deleted", expression = "java(entity.getDeleted() != 0)")
    AssetHistory toModel(AssetHistoryEntity entity);

    List<AssetHistory> toModels(List<AssetHistoryEntity> entities);
}
