package com.db.assetstore.domain.service.cmd.factory;

import com.db.assetstore.domain.service.cmd.DeleteAssetCommand;
import com.db.assetstore.infra.api.dto.AssetDeleteRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;

@Component
public class DeleteAssetCommandFactory {

    public DeleteAssetCommand createCommand(String assetId, AssetDeleteRequest request) {
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
                .executedBy(request.executedBy())
                .requestTime(Instant.now())
                .build();
    }
}
