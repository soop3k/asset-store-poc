package com.db.assetstore.domain.service;

import com.db.assetstore.domain.service.cmd.AssetCommand;
import com.db.assetstore.domain.service.cmd.CommandResult;
import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.cmd.DeleteAssetCommand;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;

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
