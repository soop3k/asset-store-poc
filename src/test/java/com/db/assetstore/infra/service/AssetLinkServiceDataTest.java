package com.db.assetstore.infra.service;

import com.db.assetstore.domain.model.asset.AssetType;
import com.db.assetstore.domain.model.link.LinkCardinality;
import com.db.assetstore.domain.service.cmd.CommandResult;
import com.db.assetstore.domain.service.link.cmd.CreateAssetLinkCommand;
import com.db.assetstore.domain.service.link.cmd.DeleteAssetLinkCommand;
import com.db.assetstore.infra.config.JsonMapperProvider;
import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.infra.jpa.AssetLinkEntity;
import com.db.assetstore.infra.jpa.LinkDefinitionEntity;
import com.db.assetstore.infra.mapper.AssetMapper;
import com.db.assetstore.infra.mapper.AssetMapperImpl;
import com.db.assetstore.infra.mapper.AttributeMapper;
import com.db.assetstore.infra.mapper.AttributesCollectionMapper;
import com.db.assetstore.infra.repository.AssetHistoryRepository;
import com.db.assetstore.infra.repository.AssetLinkRepo;
import com.db.assetstore.infra.repository.AssetRepository;
import com.db.assetstore.infra.repository.AttributeRepository;
import com.db.assetstore.infra.repository.CommandLogRepository;
import com.db.assetstore.infra.repository.LinkDefinitionRepo;
import com.db.assetstore.infra.service.cmd.CommandLogService;
import com.db.assetstore.infra.service.cmd.CommandServiceImpl;
import com.db.assetstore.infra.service.link.AssetLinkCommandValidator;
import com.db.assetstore.infra.service.link.AssetLinkService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(AssetLinkCommandValidator.class)
class AssetLinkServiceDataTest {

    @Autowired
    AssetLinkRepo assetLinkRepo;

    @Autowired
    LinkDefinitionRepo linkDefinitionRepo;

    @Autowired
    CommandLogRepository commandLogRepository;

    @Autowired
    AssetRepository assetRepository;

    @Autowired
    AttributeRepository attributeRepository;

    @Autowired
    AssetLinkCommandValidator assetLinkCommandValidator;

    @Autowired
    AssetHistoryRepository assetHistoryRepository;

    CommandServiceImpl service;

    ObjectMapper objectMapper = new JsonMapperProvider().objectMapper();

    @BeforeEach
    void setUp() {

        assetLinkRepo.deleteAll();
        assetRepository.deleteAll();
        linkDefinitionRepo.deleteAll();

        AttributesCollectionMapper collectionMapper = Mappers.getMapper(AttributesCollectionMapper.class);
        AssetMapper assetMapper = new AssetMapperImpl(collectionMapper);
        AttributeMapper attributeMapper = Mappers.getMapper(AttributeMapper.class);

        AssetService assetService = new AssetService(
                assetMapper,
                attributeMapper,
                assetRepository,
                attributeRepository,
                assetHistoryRepository);
        AssetLinkService assetLinkService = new AssetLinkService(
                assetLinkRepo,
                linkDefinitionRepo,
                assetLinkCommandValidator);
        CommandLogService commandLogService = new CommandLogService(commandLogRepository, objectMapper);

        service = new CommandServiceImpl(assetService, assetLinkService, commandLogService);

        assetRepository.save(AssetEntity.builder()
                .id("asset-1")
                .type(AssetType.CRE)
                .createdAt(Instant.parse("2023-12-31T00:00:00Z"))
                .createdBy("tester")
                .build());

        assetRepository.save(AssetEntity.builder()
                .id("asset-2")
                .type(AssetType.CRE)
                .createdAt(Instant.parse("2023-12-31T00:00:00Z"))
                .createdBy("tester")
                .build());
    }

    @Test
    void reactivatesExistingRowInsteadOfInserting() {
        linkDefinitionRepo.save(LinkDefinitionEntity.builder()
                .entityType("WORKFLOW")
                .entitySubtype("BULK")
                .cardinality(LinkCardinality.ASSET_ONE_TARGET_ONE)
                .active(true)
                .build());

        CreateAssetLinkCommand command = CreateAssetLinkCommand.builder()
                .assetId("asset-1")
                .entityType("WORKFLOW")
                .entitySubtype("BULK")
                .targetCode("WF-42")
                .executedBy("tester")
                .requestTime(Instant.parse("2024-01-01T00:00:00Z"))
                .build();

        CommandResult<Long> first = service.execute(command);
        assertThat(first.success()).isTrue();

        service.execute(DeleteAssetLinkCommand.builder()
                .assetId("asset-1")
                .entityType("WORKFLOW")
                .entitySubtype("BULK")
                .targetCode("WF-42")
                .executedBy("tester")
                .requestTime(Instant.parse("2024-01-02T00:00:00Z"))
                .build());

        AssetLinkEntity deactivated = assetLinkRepo.findById(first.result()).orElseThrow();
        assertThat(deactivated.isActive()).isFalse();
        assertThat(deactivated.getDeactivatedAt()).isNotNull();

        CommandResult<Long> second = service.execute(command);

        assertThat(second.result()).isEqualTo(first.result());
        assertThat(assetLinkRepo.count()).isEqualTo(1L);

        AssetLinkEntity reactivated = assetLinkRepo.findById(second.result()).orElseThrow();
        assertThat(reactivated.isActive()).isTrue();
        assertThat(reactivated.getDeactivatedAt()).isNull();
        assertThat(reactivated.getDeactivatedBy()).isNull();
    }

    @Test
    void deleteLink() {
        linkDefinitionRepo.save(LinkDefinitionEntity.builder()
                .entityType("WORKFLOW")
                .entitySubtype("REVALUATION")
                .cardinality(LinkCardinality.ASSET_ONE_TARGET_ONE)
                .active(true)
                .build());

        CreateAssetLinkCommand create = CreateAssetLinkCommand.builder()
                .assetId("asset-1")
                .entityType("WORKFLOW")
                .entitySubtype("REVALUATION")
                .targetCode("WF-77")
                .executedBy("tester")
                .requestTime(Instant.parse("2024-01-05T00:00:00Z"))
                .build();

        CommandResult<Long> result = service.execute(create);
        assertThat(result.success()).isTrue();

        DeleteAssetLinkCommand delete = DeleteAssetLinkCommand.builder()
                .assetId("asset-1")
                .entityType("WORKFLOW")
                .entitySubtype("REVALUATION")
                .targetCode("WF-77")
                .executedBy("auditor")
                .requestTime(Instant.parse("2024-01-06T00:00:00Z"))
                .build();

        CommandResult<Void> deleteResult = service.execute(delete);
        assertThat(deleteResult.success()).isTrue();

        AssetLinkEntity entity = assetLinkRepo.findById(result.result()).orElseThrow();
        assertThat(entity.isActive()).isFalse();
        assertThat(entity.getDeactivatedBy()).isEqualTo("auditor");
        assertThat(entity.getDeactivatedAt()).isEqualTo(Instant.parse("2024-01-06T00:00:00Z"));
    }

    @Test
    void createLinkSideCardinality() {
        linkDefinitionRepo.save(LinkDefinitionEntity.builder()
                .entityType("WORKFLOW")
                .entitySubtype("MONITORING")
                .cardinality(LinkCardinality.ASSET_MANY_TARGET_ONE)
                .active(true)
                .build());

        CreateAssetLinkCommand first = CreateAssetLinkCommand.builder()
                .assetId("asset-1")
                .entityType("WORKFLOW")
                .entitySubtype("MONITORING")
                .targetCode("WF-99")
                .executedBy("tester")
                .requestTime(Instant.parse("2024-01-03T00:00:00Z"))
                .build();

        CreateAssetLinkCommand second = CreateAssetLinkCommand.builder()
                .assetId("asset-2")
                .entityType("WORKFLOW")
                .entitySubtype("MONITORING")
                .targetCode("WF-99")
                .executedBy("tester")
                .requestTime(Instant.parse("2024-01-04T00:00:00Z"))
                .build();

        assertThat(service.execute(first).success()).isTrue();

        assertThatThrownBy(() -> service.execute(second))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target WF-99 already linked");
    }

    @Test
    void allowsMultipleLinksForAsset() {
        linkDefinitionRepo.save(LinkDefinitionEntity.builder()
                .entityType("WORKFLOW")
                .entitySubtype("CHG")
                .cardinality(LinkCardinality.ASSET_MANY_TARGET_ONE)
                .active(true)
                .build());

        CreateAssetLinkCommand first = CreateAssetLinkCommand.builder()
                .assetId("asset-1")
                .entityType("WORKFLOW")
                .entitySubtype("CHG")
                .targetCode("WF-101")
                .executedBy("tester")
                .requestTime(Instant.parse("2024-01-07T00:00:00Z"))
                .build();

        CreateAssetLinkCommand second = CreateAssetLinkCommand.builder()
                .assetId("asset-1")
                .entityType("WORKFLOW")
                .entitySubtype("CHG")
                .targetCode("WF-102")
                .executedBy("tester")
                .requestTime(Instant.parse("2024-01-08T00:00:00Z"))
                .build();

        assertThat(service.execute(first).success()).isTrue();
        assertThat(service.execute(second).success()).isTrue();

        assertThat(assetLinkRepo.activeForAssetType("asset-1", "WORKFLOW", "CHG"))
                .extracting(AssetLinkEntity::getTargetCode)
                .containsExactlyInAnyOrder("WF-101", "WF-102");
    }

    @Test
    void oneToOneLimitsBothSides() {
        linkDefinitionRepo.save(LinkDefinitionEntity.builder()
                .entityType("WORKFLOW")
                .entitySubtype("BULK")
                .cardinality(LinkCardinality.ASSET_ONE_TARGET_ONE)
                .active(true)
                .build());

        CreateAssetLinkCommand first = CreateAssetLinkCommand.builder()
                .assetId("asset-1")
                .entityType("WORKFLOW")
                .entitySubtype("BULK")
                .targetCode("WF-1")
                .executedBy("tester")
                .requestTime(Instant.parse("2024-01-09T00:00:00Z"))
                .build();

        assertThat(service.execute(first).success()).isTrue();

        CreateAssetLinkCommand second = CreateAssetLinkCommand.builder()
                .assetId("asset-1")
                .entityType("WORKFLOW")
                .entitySubtype("BULK")
                .targetCode("WF-2")
                .executedBy("tester")
                .requestTime(Instant.parse("2024-01-10T00:00:00Z"))
                .build();

        assertThatThrownBy(() -> service.execute(second))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Asset asset-1 already has an active link for WORKFLOW/BULK");

        CreateAssetLinkCommand third = CreateAssetLinkCommand.builder()
                .assetId("asset-2")
                .entityType("WORKFLOW")
                .entitySubtype("BULK")
                .targetCode("WF-1")
                .executedBy("tester")
                .requestTime(Instant.parse("2024-01-11T00:00:00Z"))
                .build();

        assertThatThrownBy(() -> service.execute(third))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target WF-1 already linked for WORKFLOW/BULK");
    }

    @Test
    void limitsAssetSideOnly() {
        linkDefinitionRepo.save(LinkDefinitionEntity.builder()
                .entityType("INSTRUMENT")
                .entitySubtype("MONITORING")
                .cardinality(LinkCardinality.ASSET_ONE_TARGET_MANY)
                .active(true)
                .build());

        CreateAssetLinkCommand first = CreateAssetLinkCommand.builder()
                .assetId("asset-1")
                .entityType("INSTRUMENT")
                .entitySubtype("MONITORING")
                .targetCode("INST-1")
                .executedBy("tester")
                .requestTime(Instant.parse("2024-01-12T00:00:00Z"))
                .build();

        assertThat(service.execute(first).success()).isTrue();

        CreateAssetLinkCommand second = CreateAssetLinkCommand.builder()
                .assetId("asset-1")
                .entityType("INSTRUMENT")
                .entitySubtype("MONITORING")
                .targetCode("INST-2")
                .executedBy("tester")
                .requestTime(Instant.parse("2024-01-13T00:00:00Z"))
                .build();

        assertThatThrownBy(() -> service.execute(second))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Asset asset-1 already has an active link for INSTRUMENT/MONITORING");

        CreateAssetLinkCommand third = CreateAssetLinkCommand.builder()
                .assetId("asset-2")
                .entityType("INSTRUMENT")
                .entitySubtype("MONITORING")
                .targetCode("INST-1")
                .executedBy("tester")
                .requestTime(Instant.parse("2024-01-14T00:00:00Z"))
                .build();

        assertThat(service.execute(third).success()).isTrue();

        assertThat(assetLinkRepo.activeForTarget("INSTRUMENT", "MONITORING", "INST-1"))
                .extracting(AssetLinkEntity::getAssetId)
                .containsExactlyInAnyOrder("asset-1", "asset-2");
    }

}
