package com.db.assetstore.domain.service.link.cmd.factory;

import com.db.assetstore.domain.service.link.cmd.CreateAssetLinkCommand;
import com.db.assetstore.infra.api.dto.AssetLinkCreateRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;

@Component
public class CreateAssetLinkCommandFactory {

    public CreateAssetLinkCommand createCommand(String assetId, AssetLinkCreateRequest request) {
        Objects.requireNonNull(assetId, "assetId");
        Objects.requireNonNull(request, "request");

        if (request.entityType() == null || request.entityType().isBlank()) {
            throw new IllegalArgumentException("Entity type is required");
        }
        if (request.entitySubtype() == null || request.entitySubtype().isBlank()) {
            throw new IllegalArgumentException("Entity subtype is required");
        }
        if (request.targetCode() == null || request.targetCode().isBlank()) {
            throw new IllegalArgumentException("Target code is required");
        }

        return CreateAssetLinkCommand.builder()
                .assetId(assetId)
                .entityType(request.entityType().trim())
                .entitySubtype(request.entitySubtype().trim())
                .targetCode(request.targetCode().trim())
                .executedBy(request.executedBy())
                .requestTime(Instant.now())
                .build();
    }
}
