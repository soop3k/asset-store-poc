package com.db.assetstore.infra.mapper;

import com.db.assetstore.domain.model.attribute.AttributeHistory;
import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.infra.jpa.AttributeHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;

import java.util.Optional;

@Mapper(componentModel = "spring")
public interface AttributeHistoryMapper {

    default AttributeHistory toModel(AttributeHistoryEntity e) {
        return new AttributeHistory(
                Optional.ofNullable(e.getAsset()).map(AssetEntity::getId).orElse(null),
                e.getName(),
                e.getValueStr(),
                e.getValueNum(),
                e.getValueBool(),
                e.getValueDate(),
                e.getValueType(),
                e.getChangedAt()
        );
    }
}
