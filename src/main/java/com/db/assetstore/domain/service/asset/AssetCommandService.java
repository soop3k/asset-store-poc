package com.db.assetstore.domain.service.asset;

import com.db.assetstore.domain.service.cmd.AssetCommand;
import com.db.assetstore.domain.service.cmd.CommandResult;
import com.db.assetstore.domain.service.asset.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.asset.cmd.DeleteAssetCommand;
import com.db.assetstore.domain.service.asset.cmd.PatchAssetCommand;

public interface AssetCommandService {

    <R> CommandResult<R> execute(AssetCommand<R> command);

    default String create(CreateAssetCommand command) {
        return execute(command).result();
    }

    default void update(PatchAssetCommand command) {
        execute(command);
    }

    default void delete(DeleteAssetCommand command) {
        execute(command);
    }
}
