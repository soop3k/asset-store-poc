package com.db.assetstore.domain.service.cmd;

import com.db.assetstore.domain.exception.command.CommandException;
import com.db.assetstore.domain.service.link.cmd.CreateAssetLinkCommand;
import com.db.assetstore.domain.service.link.cmd.DeleteAssetLinkCommand;

public interface AssetCommandVisitor {

    CommandResult<String> visit(CreateAssetCommand command) throws CommandException;

    CommandResult<Void> visit(PatchAssetCommand command) throws CommandException;

    CommandResult<Void> visit(DeleteAssetCommand command) throws CommandException;

    CommandResult<Long> visit(CreateAssetLinkCommand command) throws CommandException;

    CommandResult<Void> visit(DeleteAssetLinkCommand command) throws CommandException;
}
