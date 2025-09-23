package com.db.assetstore.infra.mapper;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.model.AssetPatch;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.infra.api.dto.AssetPatchItemRequest;
import com.db.assetstore.infra.api.dto.AssetPatchRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AssetPatchMapper {
    private final ObjectMapper mapper;
    private final AttributeJsonReader attrReader;

    public AssetPatch toPatch(AssetType type, AssetPatchRequest req) {
        List<AttributeValue<?>> avs =
                req.getAttributes() == null ? List.of() : attrReader.read(type, req.getAttributes());
        return new AssetPatch(
                req.getStatus(), req.getSubtype(), req.getNotionalAmount(), req.getYear(),
                req.getDescription(), req.getCurrency(), avs
        );
    }

    public AssetPatch toPatch(AssetType type, AssetPatchItemRequest item) {
        List<AttributeValue<?>> avs =
                item.getAttributes() == null ? List.of() : attrReader.read(type, item.getAttributes());
        return new AssetPatch(
                item.getStatus(), item.getSubtype(), item.getNotionalAmount(), item.getYear(),
                item.getDescription(), item.getCurrency(), avs
        );
    }
}