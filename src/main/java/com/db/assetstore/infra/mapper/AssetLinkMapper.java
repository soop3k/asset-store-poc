package com.db.assetstore.infra.mapper;

import com.db.assetstore.domain.model.link.AssetLink;
import com.db.assetstore.infra.jpa.link.AssetLinkEntity;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class AssetLinkMapper {

    public AssetLink toModel(AssetLinkEntity entity) {
        if (entity == null) {
            return null;
        }
        return AssetLink.builder()
                .id(entity.getId())
                .assetId(entity.getAssetId())
                .linkCode(entity.getLinkCode())
                .linkSubtype(entity.getLinkSubtype())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .active(entity.isActive())
                .deleted(entity.isDeleted())
                .validFrom(entity.getValidFrom())
                .validTo(entity.getValidTo())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .modifiedAt(entity.getModifiedAt())
                .modifiedBy(entity.getModifiedBy())
                .build();
    }

    public List<AssetLink> toModels(Collection<AssetLinkEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .filter(Objects::nonNull)
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}
