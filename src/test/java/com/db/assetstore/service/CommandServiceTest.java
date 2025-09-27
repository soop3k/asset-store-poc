package com.db.assetstore.service;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.service.cmd.CommandResult;
import com.db.assetstore.domain.service.asset.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.asset.cmd.DeleteAssetCommand;
import com.db.assetstore.domain.service.asset.cmd.PatchAssetCommand;
import com.db.assetstore.domain.service.link.cmd.CreateAssetLinkCommand;
import com.db.assetstore.domain.service.link.cmd.DeleteAssetLinkCommand;
import com.db.assetstore.infra.service.link.AssetLinkService;
import com.db.assetstore.infra.service.AssetService;
import com.db.assetstore.infra.service.cmd.CommandLogService;
import com.db.assetstore.infra.service.cmd.CommandServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CommandServiceTest {

    AssetService assetService;
    AssetLinkService assetLinkService;
    CommandLogService commandLogService;

    CommandServiceImpl service;

    @BeforeEach
    void setUp() {
        assetService = mock(AssetService.class);
        assetLinkService = mock(AssetLinkService.class);
        commandLogService = mock(CommandLogService.class);

        service = new CommandServiceImpl(assetService, assetLinkService, commandLogService);
    }

    @Test
    void executeCreateCommand() {
        CreateAssetCommand command = CreateAssetCommand.builder()
                .id("asset-1")
                .type(AssetType.CRE)
                .executedBy("tester")
                .build();

        CommandResult<String> expected = new CommandResult<>("asset-1", "asset-1");
        when(assetService.create(command)).thenReturn(expected);

        CommandResult<String> result = service.execute(command);

        assertThat(result).isEqualTo(expected);
        verify(assetService).create(command);
        verify(commandLogService).record(expected, command);
        verifyNoInteractions(assetLinkService);
    }

    @Test
    void executePatchCommand() {
        PatchAssetCommand command = PatchAssetCommand.builder()
                .assetId("asset-2")
                .executedBy("tester")
                .build();

        CommandResult<Void> expected = CommandResult.noResult("asset-2");
        when(assetService.patch(command)).thenReturn(expected);

        CommandResult<Void> result = service.execute(command);

        assertThat(result).isEqualTo(expected);
        verify(assetService).patch(command);
        verify(commandLogService).record(expected, command);
    }

    @Test
    void executeDeleteCommand() {
        DeleteAssetCommand command = DeleteAssetCommand.builder()
                .assetId("asset-3")
                .executedBy("tester")
                .build();

        CommandResult<Void> expected = CommandResult.noResult("asset-3");
        when(assetService.delete(command)).thenReturn(expected);

        CommandResult<Void> result = service.execute(command);

        assertThat(result).isEqualTo(expected);
        verify(assetService).delete(command);
        verify(commandLogService).record(expected, command);
    }

    @Test
    void executeCreateLinkCommand() {
        CreateAssetLinkCommand command = CreateAssetLinkCommand.builder()
                .assetId("asset-4")
                .entityType("WORKFLOW")
                .entitySubtype("BULK")
                .targetCode("WF-1")
                .executedBy("tester")
                .requestTime(Instant.now())
                .build();

        CommandResult<Long> expected = new CommandResult<>(42L, "asset-4");
        when(assetLinkService.create(command)).thenReturn(expected);

        CommandResult<Long> result = service.execute(command);

        assertThat(result).isEqualTo(expected);
        verify(assetLinkService).create(command);
        verify(commandLogService).record(expected, command);
        verifyNoMoreInteractions(assetService);
    }

    @Test
    void executeDeleteLinkCommand() {
        DeleteAssetLinkCommand command = DeleteAssetLinkCommand.builder()
                .assetId("asset-5")
                .entityType("WORKFLOW")
                .entitySubtype("BULK")
                .targetCode("WF-1")
                .executedBy("tester")
                .build();

        CommandResult<Void> expected = CommandResult.noResult("asset-5");
        when(assetLinkService.delete(command)).thenReturn(expected);

        CommandResult<Void> result = service.execute(command);

        assertThat(result).isEqualTo(expected);
        verify(assetLinkService).delete(command);
        verify(commandLogService).record(expected, command);
        verifyNoMoreInteractions(assetService);
    }

    @Test
    void whenCommandFails() {
        CreateAssetCommand command = CreateAssetCommand.builder()
                .id("asset-6")
                .type(AssetType.CRE)
                .build();

        CommandResult<String> failure = CommandResult.failure("asset-6");
        when(assetService.create(command)).thenReturn(failure);

        CommandResult<String> result = service.execute(command);

        assertThat(result).isEqualTo(failure);
        verify(assetService).create(command);
        verify(commandLogService, never()).record(any(), any());
    }
}
