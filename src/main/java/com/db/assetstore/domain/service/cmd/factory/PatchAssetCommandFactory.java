package com.db.assetstore.domain.service.cmd.factory;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;
import com.db.assetstore.infra.api.dto.AssetPatchRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class PatchAssetCommandFactory {

    private final AttributeJsonReader attributeJsonReader;

    public PatchAssetCommandFactory(@NonNull AttributeJsonReader attributeJsonReader) {
        this.attributeJsonReader = attributeJsonReader;
    }

    public PatchAssetCommand createCommand(@NonNull AssetType assetType,
                                           @NonNull String assetId,
                                           @NonNull AssetPatchRequest request) {

        if (assetId.isBlank()) {
            throw new IllegalArgumentException("assetId must not be blank");
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
                .requestTime(Instant.now())
                .build();
    }
}
