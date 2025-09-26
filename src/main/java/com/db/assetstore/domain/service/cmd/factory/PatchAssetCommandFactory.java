package com.db.assetstore.domain.service.cmd.factory;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;
import com.db.assetstore.domain.service.validation.AttributeValidator;
import com.db.assetstore.domain.service.validation.ValidationMode;
import com.db.assetstore.infra.api.dto.AssetPatchRequest;
import com.db.assetstore.infra.json.AttributeJsonReader;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class PatchAssetCommandFactory {

    private final AttributeValidator attributeValidator;
    private final AttributeJsonReader attributeJsonReader;

    public PatchAssetCommandFactory(@NonNull AttributeValidator attributeValidator,
                                    @NonNull AttributeJsonReader attributeJsonReader) {
        this.attributeValidator = attributeValidator;
        this.attributeJsonReader = attributeJsonReader;
    }

    public PatchAssetCommand createCommand(@NonNull AssetType assetType, @NonNull String assetId,
                                           @NonNull AssetPatchRequest request) {
        if (assetId.isBlank()) {
            throw new IllegalArgumentException("assetId must not be blank");
        }

        AttributesCollection attributes = attributeJsonReader.read(assetType, request.getAttributes());

        attributeValidator.validate(assetType, attributes, ValidationMode.PARTIAL);

        List<AttributeValue<?>> attributeValues = attributes.asListView();

        return PatchAssetCommand.builder()
                .assetId(assetId)
                .status(request.getStatus())
                .subtype(request.getSubtype())
                .notionalAmount(request.getNotionalAmount())
                .year(request.getYear())
                .description(request.getDescription())
                .currency(request.getCurrency())
                .attributes(attributeValues)
                .executedBy(request.getExecutedBy())
                .requestTime(Instant.now())
                .build();
    }
}
