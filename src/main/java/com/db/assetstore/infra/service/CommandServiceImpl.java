package com.db.assetstore.infra.service;

import com.db.assetstore.domain.service.AssetCommandService;
import com.db.assetstore.domain.service.cmd.AssetCommand;
import com.db.assetstore.domain.service.cmd.AssetCommandVisitor;
import com.db.assetstore.domain.service.cmd.CommandResult;
import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.cmd.DeleteAssetCommand;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;
import com.db.assetstore.domain.service.link.cmd.CreateAssetLinkCommand;
import com.db.assetstore.domain.service.link.cmd.DeleteAssetLinkCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandServiceImpl implements AssetCommandService, AssetCommandVisitor {

    private final AssetService assetService;
    private final AssetLinkService assetLinkService;
    private final CommandLogService commandLogService;

    @Override
    @Transactional
    public <R> CommandResult<R> execute(AssetCommand<R> command) {
        Objects.requireNonNull(command, "command");

        CommandResult<R> result = command.accept(this);
        if (result.success()) {
            commandLogService.record(result, command);
        }
        return result;
    }

    @Override
    public CommandResult<String> visit(CreateAssetCommand command) {
        return assetService.create(command);
    }

    @Override
    public CommandResult<Void> visit(PatchAssetCommand command) {
        return assetService.patch(command);
    }

    @Override
    public CommandResult<Void> visit(DeleteAssetCommand command) {
        return assetService.delete(command);
    }

    @Override
    public CommandResult<Long> visit(CreateAssetLinkCommand command) {
        return assetLinkService.create(command);
    }

    @Override
    public CommandResult<Void> visit(DeleteAssetLinkCommand command) {
        return assetLinkService.delete(command);
    }
}
