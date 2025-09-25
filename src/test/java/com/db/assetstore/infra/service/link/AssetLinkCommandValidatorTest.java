package com.db.assetstore.infra.service.link;

import com.db.assetstore.domain.exception.link.InactiveLinkDefinitionException;
import com.db.assetstore.domain.exception.link.LinkAlreadyExistsException;
import com.db.assetstore.domain.exception.link.LinkCardinalityViolationException;
import com.db.assetstore.domain.model.link.LinkCardinality;
import com.db.assetstore.domain.service.link.cmd.CreateAssetLinkCommand;
import com.db.assetstore.infra.jpa.AssetLinkEntity;
import com.db.assetstore.infra.jpa.LinkDefinitionEntity;
import com.db.assetstore.infra.repository.AssetLinkRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AssetLinkCommandValidatorTest {

    AssetLinkRepo assetLinkRepo;
    AssetLinkCommandValidator validator;

    @BeforeEach
    void setUp() {
        assetLinkRepo = mock(AssetLinkRepo.class);
        validator = new AssetLinkCommandValidator(assetLinkRepo);
    }

    @Test
    void validateCreate_whenDefinitionInactive_throwsInactiveException() {
        LinkDefinitionEntity definition = LinkDefinitionEntity.builder()
                .entityType("WORKFLOW")
                .entitySubtype("BULK")
                .cardinality(LinkCardinality.ASSET_ONE_TARGET_ONE)
                .active(false)
                .build();

        CreateAssetLinkCommand command = sampleCommand();

        assertThatThrownBy(() -> validator.validateCreate(command, definition))
                .isInstanceOf(InactiveLinkDefinitionException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    void validateCreate_whenDuplicateActiveLink_throwsLinkAlreadyExistsException() {
        LinkDefinitionEntity definition = activeDefinition();
        CreateAssetLinkCommand command = sampleCommand();

        when(assetLinkRepo.activeLink(eq("asset-1"), eq("WORKFLOW"), eq("BULK"), eq("WF-42")))
                .thenReturn(Optional.of(AssetLinkEntity.builder().id(1L).build()));

        assertThatThrownBy(() -> validator.validateCreate(command, definition))
                .isInstanceOf(LinkAlreadyExistsException.class)
                .hasMessageContaining("Active link already exists");
    }

    @Test
    void validateCreate_whenAssetSideLimited_throwsCardinalityViolation() {
        LinkDefinitionEntity definition = activeDefinition().toBuilder()
                .cardinality(LinkCardinality.ASSET_ONE_TARGET_MANY)
                .build();
        CreateAssetLinkCommand command = sampleCommand();

        when(assetLinkRepo.activeLink(any(), any(), any(), any())).thenReturn(Optional.empty());
        when(assetLinkRepo.activeForAssetType("asset-1", "WORKFLOW", "BULK"))
                .thenReturn(List.of(AssetLinkEntity.builder().id(1L).build()));
        when(assetLinkRepo.activeForTarget("WORKFLOW", "BULK", "WF-42"))
                .thenReturn(List.of());

        assertThatThrownBy(() -> validator.validateCreate(command, definition))
                .isInstanceOf(LinkCardinalityViolationException.class)
                .hasMessageContaining("Asset asset-1 already has an active link");
    }

    @Test
    void validateCreate_whenTargetSideLimited_throwsCardinalityViolation() {
        LinkDefinitionEntity definition = activeDefinition().toBuilder()
                .cardinality(LinkCardinality.ASSET_MANY_TARGET_ONE)
                .build();
        CreateAssetLinkCommand command = sampleCommand();

        when(assetLinkRepo.activeLink(any(), any(), any(), any())).thenReturn(Optional.empty());
        when(assetLinkRepo.activeForAssetType("asset-1", "WORKFLOW", "BULK"))
                .thenReturn(List.of());
        when(assetLinkRepo.activeForTarget("WORKFLOW", "BULK", "WF-42"))
                .thenReturn(List.of(AssetLinkEntity.builder().id(2L).build()));

        assertThatThrownBy(() -> validator.validateCreate(command, definition))
                .isInstanceOf(LinkCardinalityViolationException.class)
                .hasMessageContaining("Target WF-42 already linked");
    }

    private static CreateAssetLinkCommand sampleCommand() {
        return CreateAssetLinkCommand.builder()
                .assetId("asset-1")
                .entityType("WORKFLOW")
                .entitySubtype("BULK")
                .targetCode("WF-42")
                .executedBy("tester")
                .requestTime(Instant.parse("2024-01-01T00:00:00Z"))
                .build();
    }

    private static LinkDefinitionEntity activeDefinition() {
        return LinkDefinitionEntity.builder()
                .entityType("WORKFLOW")
                .entitySubtype("BULK")
                .cardinality(LinkCardinality.ASSET_ONE_TARGET_ONE)
                .active(true)
                .build();
    }
}
