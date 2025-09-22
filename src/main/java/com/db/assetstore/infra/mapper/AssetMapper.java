package com.db.assetstore.infra.mapper;

import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.infra.jpa.AttributeEntity;
import com.db.assetstore.domain.model.Asset;
import org.mapstruct.*;
import org.mapstruct.Builder;
import org.mapstruct.factory.Mappers;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {AttributeMapper.class}, builder = @Builder(disableBuilder = true))
public interface AssetMapper {

    @Mappings({
            @Mapping(target = "attributes", ignore = true)
    })
    AssetEntity toEntity(Asset asset);

    @Mappings({
            @Mapping(target = "attributes", ignore = true)
    })
    Asset toModel(AssetEntity entity);

    List<Asset> toModelList(List<AssetEntity> entities);
    List<AssetEntity> toEntityList(List<Asset> models);

    @AfterMapping
    default void fillAttributesEntity(Asset src, @MappingTarget AssetEntity dst) {
        AttributeMapper attributeMapper = Mappers.getMapper(AttributeMapper.class);
        if (dst.getAttributes() != null) {
            dst.getAttributes().clear();
        }
        var flat = src.getAttributesFlat();
        if (flat == null || flat.isEmpty()){
            return;
        }
        flat.forEach(attr -> {
            AttributeEntity ae = attributeMapper.toEntity(dst, attr);
            dst.getAttributes().add(ae);
        });
    }

    @AfterMapping
    default void fillAttributesModel(AssetEntity src, @MappingTarget Asset dst) {
        AttributeMapper attributeMapper = Mappers.getMapper(AttributeMapper.class);
        List<AttributeEntity> attrs = src.getAttributes();
        if (attrs == null || attrs.isEmpty()) {
            dst.setAttributes(Collections.emptyList());
        } else {
            dst.setAttributes(attrs.stream()
                    .map(attributeMapper::toModel)
                    .collect(Collectors.toList()));
        }
        // copy remaining simple fields not handled by builder
        dst.setVersion(src.getVersion());
        dst.setStatus(src.getStatus());
        dst.setSubtype(src.getSubtype());
        dst.setStatusEffectiveTime(src.getStatusEffectiveTime());
        dst.setCreatedBy(src.getCreatedBy());
        dst.setModifiedAt(src.getModifiedAt());
        dst.setModifiedBy(src.getModifiedBy());
        dst.setNotionalAmount(src.getNotionalAmount());
        dst.setYear(src.getYear());
        dst.setWh(src.getWh());
        dst.setSourceSystemName(src.getSourceSystemName());
        dst.setExternalReference(src.getExternalReference());
        dst.setDescription(src.getDescription());
        dst.setCurrency(src.getCurrency());
    }
}

