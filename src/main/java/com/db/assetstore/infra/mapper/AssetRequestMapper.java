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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AssetRequestMapper {

    private final AttributeJsonReader attrReader;

    public CreateAssetCommand toCreateCommand(AssetCreateRequest req, String createdBy, Instant requestTime) {
        List<AttributeValue<?>> avs = (req.attributes() == null)
                ? List.of()
                : attrReader.read(req.type(), req.attributes());
        return CreateAssetCommand.builder()
                .id(req.id())
                .type(req.type())
                .status(req.status())
                .subtype(req.subtype())
                .notionalAmount(req.notionalAmount())
                .year(req.year())
                .description(req.description())
                .currency(req.currency())
                .attributes(avs)
                .createdBy(createdBy)
                .requestTime(requestTime == null ? Instant.now() : requestTime)
                .build();
    }

    public PatchAssetCommand toPatchCommand(AssetType type, String id, AssetPatchRequest req, String modifiedBy, Instant requestTime) {
        List<AttributeValue<?>> avs = (req.getAttributes() == null)
                ? List.of()
                : attrReader.read(type, req.getAttributes());
        return PatchAssetCommand.builder()
                .assetId(new AssetId(id))
                .status(req.getStatus())
                .subtype(req.getSubtype())
                .notionalAmount(req.getNotionalAmount())
                .year(req.getYear())
                .description(req.getDescription())
                .currency(req.getCurrency())
                .attributes(avs)
                .modifiedBy(modifiedBy)
                .requestTime(requestTime == null ? Instant.now() : requestTime)
                .build();
    }

    public PatchAssetCommand toPatchCommand(AssetType type, AssetPatchItemRequest item, String modifiedBy, Instant requestTime) {
        List<AttributeValue<?>> avs = (item.getAttributes() == null)
                ? List.of()
                : attrReader.read(type, item.getAttributes());
        return PatchAssetCommand.builder()
                .assetId(new AssetId(item.getId()))
                .status(item.getStatus())
                .subtype(item.getSubtype())
                .notionalAmount(item.getNotionalAmount())
                .year(item.getYear())
                .description(item.getDescription())
                .currency(item.getCurrency())
                .attributes(avs)
                .modifiedBy(modifiedBy)
                .requestTime(requestTime == null ? Instant.now() : requestTime)
                .build();
    }
}
