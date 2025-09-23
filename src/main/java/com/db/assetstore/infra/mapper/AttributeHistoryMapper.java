package com.db.assetstore.infra.mapper;

import com.db.assetstore.domain.model.attribute.AttributeHistory;
import com.db.assetstore.infra.jpa.AttributeHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;

/**
 * Maps JPA AttributeHistoryEntity to domain AttributeHistory.
 */
@Mapper(componentModel = "spring")
public interface AttributeHistoryMapper {

    default AttributeHistory toModel(AttributeHistoryEntity e) {
        if (e == null) return null;
        String assetId = e.getAsset() != null ? e.getAsset().getId() : null;
        return new AttributeHistory(
                assetId,
                e.getName(),
                e.getValueStr(),
                e.getValueNum(),
                e.getValueBool(),
                e.getValueType(),
                e.getChangedAt()
        );
    }
}
