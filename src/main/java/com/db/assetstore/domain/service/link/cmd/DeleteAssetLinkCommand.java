package com.db.assetstore.domain.service.link.cmd;

import com.db.assetstore.domain.service.cmd.AssetCommand;
import com.db.assetstore.domain.service.cmd.AssetCommandVisitor;
import com.db.assetstore.domain.service.cmd.CommandResult;
import lombok.Builder;

import java.time.Instant;

@Builder
public record DeleteAssetLinkCommand(
        String assetId,
        String entityType,
        String entitySubtype,
        String targetCode,
        String executedBy,
        Instant requestTime
) implements AssetCommand<Void> {

    @Override
    public CommandResult<Void> accept(AssetCommandVisitor visitor) {
        return AssetCommand.super.requireVisitor(visitor).visit(this);
    }
}
