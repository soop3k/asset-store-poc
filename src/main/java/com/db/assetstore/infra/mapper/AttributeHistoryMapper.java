package com.db.assetstore.infra.mapper;

import com.db.assetstore.domain.model.attribute.AttributeHistory;
import com.db.assetstore.infra.jpa.AttributeHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AttributeHistoryMapper {

    @Mapping(target = "assetId", expression = "java(entity.getAsset() != null ? entity.getAsset().getId() : null)")
    AttributeHistory toModel(AttributeHistoryEntity entity);

    List<AttributeHistory> toModels(List<AttributeHistoryEntity> entities);
}
