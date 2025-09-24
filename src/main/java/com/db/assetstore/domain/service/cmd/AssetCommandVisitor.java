package com.db.assetstore.domain.service.cmd;

public interface AssetCommandVisitor {

    CommandResult<String> visit(CreateAssetCommand command);

    CommandResult<Void> visit(PatchAssetCommand command);

    CommandResult<Void> visit(DeleteAssetCommand command);
}
