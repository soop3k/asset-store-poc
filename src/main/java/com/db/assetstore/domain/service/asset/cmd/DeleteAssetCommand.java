package com.db.assetstore.domain.service.asset.cmd;

import com.db.assetstore.domain.service.cmd.AssetCommand;
import com.db.assetstore.domain.service.cmd.AssetCommandVisitor;
import com.db.assetstore.domain.service.cmd.CommandResult;
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
