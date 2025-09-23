package com.db.assetstore.infra.mapper;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;
import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
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

    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "requestTime", expression = "java(requestTime != null ? requestTime : java.time.Instant.now())")
    public abstract PatchAssetCommand toPatchCommand(AssetType type, String id, AssetPatchRequest req, String modifiedBy, Instant requestTime);

    @Mapping(target = "assetId", source = "item.id")
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "requestTime", expression = "java(requestTime != null ? requestTime : java.time.Instant.now())")
    public abstract PatchAssetCommand toPatchCommand(AssetType type, AssetPatchRequest item, String modifiedBy, Instant requestTime);

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
    protected void setPatchItemAttributes(AssetType type, AssetPatchRequest item, @MappingTarget PatchAssetCommand.PatchAssetCommandBuilder command) {
        List<AttributeValue<?>> avs = (item.getAttributes() == null) ? List.of() : attrReader.read(type, item.getAttributes());
        command.attributes(avs);
    }
}
