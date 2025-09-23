package com.db.assetstore.infra.service;

import com.db.assetstore.domain.service.cmd.AssetCommand;
import com.db.assetstore.domain.service.cmd.CommandResult;
import com.db.assetstore.infra.config.JsonMapperProvider;
import com.db.assetstore.infra.jpa.CommandLogEntity;
import com.db.assetstore.infra.repository.CommandLogRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AssetCommandServiceCommandLogDataTest {

    @Autowired
    CommandLogRepository commandLogRepository;

    ObjectMapper objectMapper = new JsonMapperProvider().objectMapper();

    AssetCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AssetCommandServiceImpl(null, null, null, null, commandLogRepository, objectMapper) {
            @Override
            @SuppressWarnings("unchecked")
            protected <R> CommandResult<R> doExecute(AssetCommand<R> command) {
                if (command instanceof LoggingOnlyCommand loggingOnlyCommand) {
                    return (CommandResult<R>) CommandResult.noResult(loggingOnlyCommand.assetId());
                }
                return super.doExecute(command);
            }
        };
    }

    @Test
    void execute_persistsCommandLogEntryWithSerializedPayload() throws Exception {
        LoggingOnlyCommand command = new LoggingOnlyCommand("asset-123", "payload-data", Instant.parse("2024-01-01T00:00:00Z"));

        CommandResult<Void> result = service.execute(command);

        assertNull(result.result());
        assertEquals("asset-123", result.assetId());

        List<CommandLogEntity> entries = commandLogRepository.findAll();
        assertEquals(1, entries.size(), "exactly one command log entry should be persisted");
        CommandLogEntity entry = entries.get(0);

        assertEquals("LoggingOnlyCommand", entry.getCommandType());
        assertEquals("asset-123", entry.getAssetId());
        assertNotNull(entry.getCreatedAt());

        JsonNode payload = objectMapper.readTree(entry.getPayload());
        assertEquals("asset-123", payload.get("assetId").asText());
        assertEquals("payload-data", payload.get("data").asText());
        assertEquals("2024-01-01T00:00:00Z", payload.get("requestTime").asText());
    }

    record LoggingOnlyCommand(String assetId, String data, Instant requestTime) implements AssetCommand<Void> { }
}
