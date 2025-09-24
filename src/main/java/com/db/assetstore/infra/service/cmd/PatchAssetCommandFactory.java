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
        this.attributeJsonReader = Objects.requireNonNull(attributeJsonReader, "attributeJsonReader");
    }

    public PatchAssetCommand create(AssetType assetType, String assetId, AssetPatchRequest request) {
        Objects.requireNonNull(assetType, "assetType");
        if (assetId == null || assetId.isBlank()) {
            throw new IllegalArgumentException("assetId must not be blank");
        }
        Objects.requireNonNull(request, "request");

        if (request.getExecutedBy() == null || request.getExecutedBy().isBlank()) {
            throw new IllegalArgumentException("Patch request must include executedBy");
        }

        List<AttributeValue<?>> attributes = request.getAttributes() == null
                ? List.of()
                : List.copyOf(attributeJsonReader.read(assetType, request.getAttributes()));

        return PatchAssetCommand.builder()
                .assetId(assetId)
                .status(request.getStatus())
                .subtype(request.getSubtype())
                .notionalAmount(request.getNotionalAmount())
                .year(request.getYear())
                .description(request.getDescription())
                .currency(request.getCurrency())
                .attributes(attributes)
                .executedBy(request.getExecutedBy())
                .build();
    }

    public PatchAssetCommand create(AssetType assetType, AssetPatchRequest request) {
        Objects.requireNonNull(request, "request");
        String assetId = request.getId();
        if (assetId == null || assetId.isBlank()) {
            throw new IllegalArgumentException("Patch request must contain an asset id");
        }
        return create(assetType, assetId, request);
    }
}
