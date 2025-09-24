package com.db.assetstore.domain.service.cmd;

import lombok.Builder;

import java.time.Instant;

/**
 * Command representing a request to delete an asset.
 */
@Builder
public record DeleteAssetCommand(
        String assetId,
        String executedBy,
        Instant requestTime
) implements AssetCommand<Void> {

    @Override
    public CommandResult<Void> accept(AssetCommandVisitor visitor) {
        return requireVisitor(visitor).visit(this);
    }

    @Override
    public String executedBy() {
        return executedBy;
    }
}
