package com.db.assetstore.infra.service.cmd;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.cmd.DeleteAssetCommand;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
import com.db.assetstore.infra.api.dto.AssetDeleteRequest;
import com.db.assetstore.infra.api.dto.AssetPatchRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Component
public class AssetCommandFactory {

    private final AttributeJsonReader attributeJsonReader;

    public AssetCommandFactory(AttributeJsonReader attributeJsonReader) {
        this.attributeJsonReader = attributeJsonReader;
    }

    public CreateAssetCommand createCreateCommand(AssetCreateRequest request) {
        Objects.requireNonNull(request, "request");

        List<AttributeValue<?>> attributes = request.attributes() == null
                ? List.of()
                : attributeJsonReader.read(request.type(), request.attributes());

        return CreateAssetCommand.builder()
                .id(request.id())
                .type(request.type())
                .status(request.status())
                .subtype(request.subtype())
                .notionalAmount(request.notionalAmount())
                .year(request.year())
                .description(request.description())
                .currency(request.currency())
                .attributes(attributes)
                .build();
    }

    public PatchAssetCommand createPatchCommand(AssetType assetType, String assetId, AssetPatchRequest request) {
        Objects.requireNonNull(assetType, "assetType");
        Objects.requireNonNull(assetId, "assetId");
        if (assetId.isBlank()) {
            throw new IllegalArgumentException("assetId must not be blank");
        }
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

    public PatchAssetCommand createPatchCommand(AssetType assetType, AssetPatchRequest request) {
        Objects.requireNonNull(request, "request");
        String id = request.getId();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Patch request must contain an asset id");
        }
        return createPatchCommand(assetType, id, request);
    }

    public DeleteAssetCommand createDeleteCommand(String assetId, AssetDeleteRequest request) {
        Objects.requireNonNull(assetId, "assetId");
        if (assetId.isBlank()) {
            throw new IllegalArgumentException("assetId must not be blank");
        }
        Objects.requireNonNull(request, "request");
        if (request.id() == null || request.id().isBlank()) {
            throw new IllegalArgumentException("Delete request must include an asset id");
        }
        if (!assetId.equals(request.id())) {
            throw new IllegalArgumentException("Delete request id must match path asset id");
        }

        return DeleteAssetCommand.builder()
                .assetId(assetId)
                .deletedBy(request.deletedBy())
                .requestTime(Instant.now())
                .build();
    }
}
