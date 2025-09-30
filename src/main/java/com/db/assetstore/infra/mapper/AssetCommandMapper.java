package com.db.assetstore.infra.mapper;

import com.db.assetstore.domain.model.asset.Asset;
import com.db.assetstore.domain.model.asset.AssetPatch;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.domain.service.asset.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.asset.cmd.PatchAssetCommand;
import org.mapstruct.AfterMapping;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

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
    @Mapping(target = "attributes", ignore = true)
    Asset fromCreateCommand(CreateAssetCommand command, String assetId, Instant createdAt, Instant modifiedAt);

    @AfterMapping
    default void mapAttributes(CreateAssetCommand command, @MappingTarget Asset.AssetBuilder builder) {
        List<AttributeValue<?>> attributes = command.attributes();
        if (attributes == null || attributes.isEmpty()) {
            builder.attributes(AttributesCollection.empty());
            return;
        }
        builder.attributes(AttributesCollection.fromFlat(attributes));
    }

    @Mapping(target = "attributes", source = "attributes")
    AssetPatch toPatch(PatchAssetCommand command);
}
