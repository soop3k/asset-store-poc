package com.db.assetstore.domain.service.cmd;

import com.db.assetstore.domain.service.link.cmd.CreateAssetLinkCommand;
import com.db.assetstore.domain.service.link.cmd.DeleteAssetLinkCommand;

public interface AssetCommandVisitor {

    CommandResult<String> visit(CreateAssetCommand command);

    CommandResult<Void> visit(PatchAssetCommand command);

    CommandResult<Void> visit(DeleteAssetCommand command);

    CommandResult<Long> visit(CreateAssetLinkCommand command);

    CommandResult<Void> visit(DeleteAssetLinkCommand command);
}
