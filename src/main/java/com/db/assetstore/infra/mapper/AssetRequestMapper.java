package com.db.assetstore.infra.mapper;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.service.CreateAssetCommand;
import com.db.assetstore.domain.service.PatchAssetCommand;
import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.model.AssetId;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
import com.db.assetstore.infra.api.dto.AssetPatchItemRequest;
import com.db.assetstore.infra.api.dto.AssetPatchRequest;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class AssetRequestMapper {

    @Autowired
    protected AttributeJsonReader attrReader;

    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "requestTime", expression = "java(requestTime != null ? requestTime : java.time.Instant.now())")
    public abstract CreateAssetCommand toCreateCommand(AssetCreateRequest req, String createdBy, Instant requestTime);

    @Mapping(target = "assetId", expression = "java(new com.db.assetstore.domain.model.AssetId(id))")
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "requestTime", expression = "java(requestTime != null ? requestTime : java.time.Instant.now())")
    public abstract PatchAssetCommand toPatchCommand(AssetType type, String id, AssetPatchRequest req, String modifiedBy, Instant requestTime);

    @Mapping(target = "assetId", expression = "java(new com.db.assetstore.domain.model.AssetId(item.getId()))")
    @Mapping(target = "attributes", ignore = true)  
    @Mapping(target = "requestTime", expression = "java(requestTime != null ? requestTime : java.time.Instant.now())")
    public abstract PatchAssetCommand toPatchCommand(AssetType type, AssetPatchItemRequest item, String modifiedBy, Instant requestTime);

    @AfterMapping
    protected void setCreateAttributes(AssetCreateRequest req, @MappingTarget CreateAssetCommand.CreateAssetCommandBuilder command) {
        List<AttributeValue<?>> avs = (req.attributes() == null) ? List.of() : attrReader.read(req.type(), req.attributes());
        command.attributes(avs);
    }

    @AfterMapping
    protected void setPatchAttributes(AssetType type, AssetPatchRequest req, @MappingTarget PatchAssetCommand.PatchAssetCommandBuilder command) {
        List<AttributeValue<?>> avs = (req.getAttributes() == null) ? List.of() : attrReader.read(type, req.getAttributes());
        command.attributes(avs);
    }

    @AfterMapping
    protected void setPatchItemAttributes(AssetType type, AssetPatchItemRequest item, @MappingTarget PatchAssetCommand.PatchAssetCommandBuilder command) {
        List<AttributeValue<?>> avs = (item.getAttributes() == null) ? List.of() : attrReader.read(type, item.getAttributes());
        command.attributes(avs);
    }
}
