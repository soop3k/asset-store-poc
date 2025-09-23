package com.db.assetstore.infra.service.cmd;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;
import com.db.assetstore.infra.api.dto.AssetPatchRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class PatchAssetCommandFactory {

    private final AttributeJsonReader attributeJsonReader;

    public PatchAssetCommandFactory(AttributeJsonReader attributeJsonReader) {
        this.attributeJsonReader = attributeJsonReader;
    }

    public PatchAssetCommand createCommand(AssetType assetType, String assetId, AssetPatchRequest request) {
        Objects.requireNonNull(assetType, "assetType");
        Objects.requireNonNull(assetId, "assetId");
        Objects.requireNonNull(request, "request");

        List<AttributeValue<?>> attributes = request.getAttributes() == null
                ? List.of()
                : attributeJsonReader.read(assetType, request.getAttributes());

        return PatchAssetCommand.builder()
                .assetId(assetId)
                .status(request.getStatus())
                .subtype(request.getSubtype())
                .notionalAmount(request.getNotionalAmount())
                .year(request.getYear())
                .description(request.getDescription())
                .currency(request.getCurrency())
                .attributes(attributes)
                .build();
    }
}
