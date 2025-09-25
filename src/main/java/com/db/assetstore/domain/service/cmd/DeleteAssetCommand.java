package com.db.assetstore.domain.service.cmd;

import lombok.Builder;
import lombok.NonNull;

import java.time.Instant;

/**
 * Command representing a request to delete an asset.
 */
@Builder
public record DeleteAssetCommand(
        @NonNull String assetId,
        String executedBy,
        Instant requestTime
) implements AssetCommand<Void> {

    @Override
    public CommandResult<Void> accept(AssetCommandVisitor visitor) {
        return requireVisitor(visitor).visit(this);
    }

}
