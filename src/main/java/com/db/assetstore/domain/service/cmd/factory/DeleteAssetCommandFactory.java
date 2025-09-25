package com.db.assetstore.domain.service.cmd.factory;

import com.db.assetstore.domain.service.cmd.DeleteAssetCommand;
import com.db.assetstore.infra.api.dto.AssetDeleteRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DeleteAssetCommandFactory {

    public DeleteAssetCommand createCommand(@NonNull String assetId, @NonNull AssetDeleteRequest request) {
        if (assetId.isBlank()) {
            throw new IllegalArgumentException("assetId must not be blank");
        }
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
