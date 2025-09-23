package com.db.assetstore.infra.mapper;

import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public abstract class AssetCreateMapper {
    
    @Autowired
    protected AttributeJsonReader attrReader;

    @Mapping(target = "id", source = "req", qualifiedByName = "generateId")
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "attributes", source = "req", qualifiedByName = "mapAttributes")
    public abstract Asset toAsset(AssetCreateRequest req);

    @AfterMapping
    protected void setAdditionalFields(AssetCreateRequest req, @MappingTarget Asset asset) {
        asset.setStatus(req.status());
        asset.setSubtype(req.subtype());
        asset.setNotionalAmount(req.notionalAmount());
        asset.setYear(req.year());
        asset.setDescription(req.description());
        asset.setCurrency(req.currency());
    }

    @Named("generateId")
    protected String generateId(AssetCreateRequest req) {
        return (req.id() != null && !req.id().isBlank()) ? req.id() : UUID.randomUUID().toString();
    }

    @Named("mapAttributes")
    protected AttributesCollection mapAttributes(AssetCreateRequest req) {
        List<AttributeValue<?>> avs = req.attributes() == null ? List.of() : attrReader.read(req.type(), req.attributes());
        return AttributesCollection.fromFlat(avs);
    }
}