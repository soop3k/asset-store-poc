package com.db.assetstore.infra.mapper;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.model.AssetPatch;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.infra.api.dto.AssetPatchItemRequest;
import com.db.assetstore.infra.api.dto.AssetPatchRequest;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class AssetPatchMapper {
    
    @Autowired
    protected AttributeJsonReader attrReader;

    @Mapping(target = "attributes", ignore = true)
    public abstract AssetPatch toPatch(AssetType type, AssetPatchRequest req);

    @Mapping(target = "attributes", ignore = true) 
    public abstract AssetPatch toPatch(AssetType type, AssetPatchItemRequest item);

    @AfterMapping
    protected void setPatchRequestAttributes(AssetType type, AssetPatchRequest req, @MappingTarget AssetPatch.AssetPatchBuilder builder) {
        List<AttributeValue<?>> avs = req.getAttributes() == null ? List.of() : attrReader.read(type, req.getAttributes());
        builder.attributes(avs);
    }

    @AfterMapping
    protected void setPatchItemAttributes(AssetType type, AssetPatchItemRequest item, @MappingTarget AssetPatch.AssetPatchBuilder builder) {
        List<AttributeValue<?>> avs = item.getAttributes() == null ? List.of() : attrReader.read(type, item.getAttributes());
        builder.attributes(avs);
    }
}