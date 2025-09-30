package com.db.assetstore.infra.service;

import com.db.assetstore.domain.model.asset.AssetType;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.service.cmd.AssetCommand;
import com.db.assetstore.domain.service.cmd.AssetCommandVisitor;
import com.db.assetstore.domain.service.cmd.CommandResult;
import com.db.assetstore.domain.service.asset.cmd.CreateAssetCommand;
import com.db.assetstore.infra.config.JsonMapperProvider;
import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.infra.jpa.AttributeEntity;
import com.db.assetstore.infra.jpa.CommandLogEntity;
import com.db.assetstore.infra.repository.CommandLogRepository;
import com.db.assetstore.infra.repository.AssetHistoryRepository;
import com.db.assetstore.infra.repository.AssetLinkRepo;
import com.db.assetstore.infra.repository.AssetRepository;
import com.db.assetstore.infra.repository.AttributeRepository;
import com.db.assetstore.infra.repository.LinkDefinitionRepo;
import com.db.assetstore.infra.service.cmd.CommandLogService;
import com.db.assetstore.infra.service.cmd.CommandServiceImpl;
import com.db.assetstore.infra.mapper.AssetHistoryMapper;
import com.db.assetstore.infra.mapper.AssetMapper;
import com.db.assetstore.infra.mapper.AssetMapperImpl;
import com.db.assetstore.infra.mapper.AttributeMapper;
import com.db.assetstore.infra.mapper.AttributesCollectionMapper;
import com.db.assetstore.infra.service.link.AssetLinkCommandValidator;
import com.db.assetstore.infra.service.link.AssetLinkService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AssetCommandServiceCommandLogDataTest {

    @Autowired
    CommandLogRepository commandLogRepository;

    @Autowired
    AssetRepository assetRepository;

    @Autowired
    AttributeRepository attributeRepository;

    @Autowired
    AssetLinkRepo assetLinkRepo;

    @Autowired
    LinkDefinitionRepo linkDefinitionRepo;

    @Autowired
    AssetHistoryRepository assetHistoryRepository;

    ObjectMapper objectMapper = new JsonMapperProvider().objectMapper();

    CommandServiceImpl service;

    @BeforeEach
    void setUp() {
        AttributesCollectionMapper collectionMapper = Mappers.getMapper(AttributesCollectionMapper.class);
        AssetMapper assetMapper = new AssetMapperImpl(collectionMapper);
        AssetHistoryMapper assetHistoryMapper = Mappers.getMapper(AssetHistoryMapper.class);
        AttributeMapper attributeMapper = Mappers.getMapper(AttributeMapper.class);
        AssetLinkCommandValidator assetLinkCommandValidator = new AssetLinkCommandValidator(assetLinkRepo);
        AssetService assetService = new AssetService(
                assetMapper,
                attributeMapper,
                assetRepository,
                attributeRepository,
                assetHistoryRepository,
                assetHistoryMapper);
        AssetLinkService assetLinkService = new AssetLinkService(
                assetLinkRepo,
                linkDefinitionRepo,
                assetLinkCommandValidator);
        CommandLogService commandLogService = new CommandLogService(commandLogRepository, objectMapper);

        service = new CommandServiceImpl(assetService, assetLinkService, commandLogService);
    }

    @Test
    void persistsAssetAndCommandLogEntry() throws Exception {
        CreateAssetCommand command = CreateAssetCommand.builder()
                .id("asset-123")
                .type(AssetType.CRE)
                .status("ACTIVE")
                .description("Primary asset")
                .attributes(List.of(
                        new AVString("name", "HQ Building"),
                        new AVDecimal("notional", new BigDecimal("123.45"))
                ))
                .executedBy("auditor")
                .requestTime(Instant.parse("2024-01-01T00:00:00Z"))
                .build();

        CommandResult<String> result = service.execute(command);

        assertEquals("asset-123", result.result());
        assertEquals("asset-123", result.assetId());

        AssetEntity persisted = assetRepository.findById("asset-123")
                .orElseThrow(() -> new AssertionError("asset should be persisted"));
        assertEquals("Primary asset", persisted.getDescription());
        assertEquals("auditor", persisted.getCreatedBy());
        assertEquals(2, persisted.getAttributes().size());
        assertTrue(persisted.getAttributes().stream()
                .map(AttributeEntity::getName)
                .collect(Collectors.toSet())
                .containsAll(List.of("name", "notional")));

        List<CommandLogEntity> entries = commandLogRepository.findAll();
        assertEquals(1, entries.size(), "exactly one command log entry should be persisted");
        CommandLogEntity entry = entries.get(0);

        assertEquals("CreateAssetCommand", entry.getCommandType());
        assertEquals("asset-123", entry.getAssetId());
        assertNotNull(entry.getCreatedAt());

        JsonNode payload = objectMapper.readTree(entry.getPayload());
        assertEquals("asset-123", payload.get("id").asText());
        assertEquals("auditor", payload.get("executedBy").asText());
    }

    @Test
    void commandReportsFailure() {
        FailingCommand command = new FailingCommand("asset-456", "auditor");

        CommandResult<Void> result = service.execute(command);

        assertNull(result.result());
        assertEquals("asset-456", result.assetId());
        assertFalse(result.success());

        assertTrue(commandLogRepository.findAll().isEmpty(), "no command log entry should be persisted for failed command");
    }

    record FailingCommand(String assetId, String executedBy) implements AssetCommand<Void> {

        @Override
        public CommandResult<Void> accept(AssetCommandVisitor visitor) {
            return CommandResult.failure(assetId);
        }
    }

}
