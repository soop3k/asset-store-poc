package com.db.assetstore.infra.mapper;

import com.db.assetstore.domain.model.asset.Asset;
import com.db.assetstore.domain.model.asset.AssetPatch;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.domain.service.asset.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.asset.cmd.PatchAssetCommand;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.util.List;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface AssetCommandMapper {

    @Mapping(target = "id", source = "assetId")
    @Mapping(target = "type", source = "command.type")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "modifiedAt", source = "modifiedAt")
    @Mapping(target = "status", source = "command.status")
    @Mapping(target = "subtype", source = "command.subtype")
    @Mapping(target = "notionalAmount", source = "command.notionalAmount")
    @Mapping(target = "year", source = "command.year")
    @Mapping(target = "description", source = "command.description")
    @Mapping(target = "currency", source = "command.currency")
    @Mapping(target = "createdBy", source = "command.executedBy")
    @Mapping(target = "modifiedBy", source = "command.executedBy")
    @Mapping(target = "attributes", source = "command.attributes")
    Asset fromCreateCommand(CreateAssetCommand command, String assetId, Instant createdAt, Instant modifiedAt);

    @Mapping(target = "attributes", source = "attributes")
    AssetPatch toPatch(PatchAssetCommand command);

    default AttributesCollection mapAttributes(List<AttributeValue<?>> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return AttributesCollection.empty();
        }
        return AttributesCollection.fromFlat(attributes);
    }
}
