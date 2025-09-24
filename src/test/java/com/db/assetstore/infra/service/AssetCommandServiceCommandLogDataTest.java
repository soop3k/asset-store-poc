package com.db.assetstore.infra.service;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.service.cmd.AssetCommand;
import com.db.assetstore.domain.service.cmd.AssetCommandVisitor;
import com.db.assetstore.domain.service.cmd.CommandResult;
import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.infra.jpa.AttributeEntity;
import com.db.assetstore.infra.config.JsonMapperProvider;
import com.db.assetstore.infra.jpa.CommandLogEntity;
import com.db.assetstore.infra.repository.CommandLogRepository;
import com.db.assetstore.infra.repository.AssetRepository;
import com.db.assetstore.infra.repository.AttributeRepository;
import com.db.assetstore.infra.service.AssetCommandServiceImpl;
import com.db.assetstore.infra.mapper.AssetMapper;
import com.db.assetstore.infra.mapper.AttributeMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    ObjectMapper objectMapper = new JsonMapperProvider().objectMapper();

    AssetCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        AttributeMapper attributeMapper = new AttributeMapper() {};
        service = new AssetCommandServiceImpl(
                new SimpleAssetMapper(),
                attributeMapper,
                assetRepository,
                attributeRepository,
                commandLogRepository,
                objectMapper
        );
    }

    @Test
    void execute_createCommand_persistsAssetAndCommandLogEntry() throws Exception {
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
    void execute_whenCommandReportsFailure_doesNotPersistLog() {
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

    private static final class SimpleAssetMapper implements AssetMapper {

        @Override
        public Asset toModel(AssetEntity entity) {
            if (entity == null) {
                return null;
            }
            Asset asset = Asset.builder()
                    .id(entity.getId())
                    .type(entity.getType())
                    .status(entity.getStatus())
                    .subtype(entity.getSubtype())
                    .description(entity.getDescription())
                    .currency(entity.getCurrency())
                    .createdBy(entity.getCreatedBy())
                    .modifiedBy(entity.getModifiedBy())
                    .build();
            if (entity.getAttributes() != null) {
                asset.setAttributes(entity.getAttributes().stream()
                        .map(a -> new AVString(a.getName(), a.getValueStr()))
                        .collect(Collectors.toList()));
            }
            return asset;
        }

        @Override
        public AssetEntity toEntity(Asset asset) {
            if (asset == null) {
                return null;
            }
            return AssetEntity.builder()
                    .id(asset.getId())
                    .type(asset.getType())
                    .status(asset.getStatus())
                    .subtype(asset.getSubtype())
                    .description(asset.getDescription())
                    .currency(asset.getCurrency())
                    .createdBy(asset.getCreatedBy())
                    .modifiedBy(asset.getModifiedBy())
                    .createdAt(asset.getCreatedAt())
                    .modifiedAt(asset.getModifiedAt())
                    .notionalAmount(asset.getNotionalAmount())
                    .year(asset.getYear())
                    .attributes(new java.util.ArrayList<>())
                    .build();
        }

        @Override
        public List<Asset> toModelList(List<AssetEntity> entities) {
            if (entities == null) {
                return List.of();
            }
            return entities.stream().map(this::toModel).collect(Collectors.toList());
        }

        @Override
        public List<AssetEntity> toEntityList(List<Asset> models) {
            if (models == null) {
                return List.of();
            }
            return models.stream().map(this::toEntity).collect(Collectors.toList());
        }
    }
}
