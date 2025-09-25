package com.db.assetstore.domain.service.link.cmd;

import com.db.assetstore.domain.service.cmd.AssetCommand;
import com.db.assetstore.domain.service.cmd.AssetCommandVisitor;
import com.db.assetstore.domain.service.cmd.CommandResult;
import lombok.Builder;

import java.time.Instant;

@Builder
public record CreateAssetLinkCommand(
        String assetId,
        String entityType,
        String entitySubtype,
        String targetCode,
        String executedBy,
        Instant requestTime
) implements AssetCommand<Long> {

    @Override
    public CommandResult<Long> accept(AssetCommandVisitor visitor) {
        return AssetCommand.super.requireVisitor(visitor).visit(this);
    }
}
