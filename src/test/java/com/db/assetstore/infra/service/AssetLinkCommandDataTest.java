package com.db.assetstore.infra.service;

import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.link.LinkCardinality;
import com.db.assetstore.domain.service.cmd.CommandResult;
import com.db.assetstore.domain.service.link.cmd.CreateAssetLinkCommand;
import com.db.assetstore.domain.service.link.cmd.DeleteAssetLinkCommand;
import com.db.assetstore.infra.config.JsonMapperProvider;
import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.infra.jpa.AssetLinkEntity;
import com.db.assetstore.infra.jpa.LinkDefinitionEntity;
import com.db.assetstore.infra.mapper.AssetMapper;
import com.db.assetstore.infra.mapper.AttributeMapper;
import com.db.assetstore.infra.repository.AssetLinkRepo;
import com.db.assetstore.infra.repository.AssetRepository;
import com.db.assetstore.infra.repository.AttributeRepository;
import com.db.assetstore.infra.repository.CommandLogRepository;
import com.db.assetstore.infra.repository.LinkDefinitionRepo;
import com.db.assetstore.infra.service.link.AssetLinkCommandValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(AssetLinkCommandValidator.class)
class AssetLinkCommandDataTest {

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

    AssetCommandServiceImpl service;

    ObjectMapper objectMapper = new JsonMapperProvider().objectMapper();

    @BeforeEach
    void setUp() {
        service = new AssetCommandServiceImpl(
                new SimpleAssetMapper(),
                new AttributeMapper() {},
                assetRepository,
                attributeRepository,
                commandLogRepository,
                assetLinkRepo,
                linkDefinitionRepo,
                assetLinkCommandValidator,
                objectMapper
        );
    }

    @Test
    void createLink_reactivatesExistingRowInsteadOfInserting() {
        linkDefinitionRepo.save(LinkDefinitionEntity.builder()
                .entityType("WORKFLOW")
                .entitySubtype("BULK")
                .cardinality(LinkCardinality.ONE_TO_ONE)
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
    void createLink_respectsTargetSideCardinality() {
        linkDefinitionRepo.save(LinkDefinitionEntity.builder()
                .entityType("WORKFLOW")
                .entitySubtype("MONITORING")
                .cardinality(LinkCardinality.ONE_TO_MANY)
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

    private static final class SimpleAssetMapper implements AssetMapper {
        @Override
        public Asset toModel(AssetEntity entity) {
            return null;
        }

        @Override
        public AssetEntity toEntity(Asset asset) {
            return null;
        }

        @Override
        public List<Asset> toModelList(List<AssetEntity> entities) {
            return List.of();
        }

        @Override
        public List<AssetEntity> toEntityList(List<Asset> models) {
            return List.of();
        }
    }
}
