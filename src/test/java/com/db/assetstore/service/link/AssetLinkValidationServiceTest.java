package com.db.assetstore.service.link;

import com.db.assetstore.domain.model.link.LinkCardinality;
import com.db.assetstore.domain.service.link.cmd.CreateAssetLinkCommand;
import com.db.assetstore.infra.jpa.link.LinkDefinitionEntity;
import com.db.assetstore.infra.repository.link.AssetLinkRepository;
import com.db.assetstore.infra.service.link.AssetLinkValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AssetLinkValidationServiceTest {

    AssetLinkRepository assetLinkRepository;
    AssetLinkValidationService validationService;

    @BeforeEach
    void setUp() {
        assetLinkRepository = mock(AssetLinkRepository.class);
        validationService = new AssetLinkValidationService(assetLinkRepository);
    }

    @Test
    void validate_whenDefinitionDisabled_shouldThrow() {
        LinkDefinitionEntity definition = baseDefinition().toBuilder().enabled(false).build();
        definition.setAllowedEntityTypes(Set.of("WORKFLOW"));

        CreateAssetLinkCommand command = CreateAssetLinkCommand.builder()
                .assetId("A1")
                .entityType("WORKFLOW")
                .entityId("WF-1")
                .linkSubtype("BULK")
                .build();

        assertThrows(IllegalStateException.class, () -> validationService.validate(definition, command));
    }

    @Test
    void validate_whenEntityTypeUnknown_shouldThrow() {
        LinkDefinitionEntity definition = baseDefinition();
        definition.setAllowedEntityTypes(Set.of("WORKFLOW"));

        CreateAssetLinkCommand command = CreateAssetLinkCommand.builder()
                .assetId("A1")
                .entityType("INSTRUMENT")
                .entityId("WF-1")
                .linkSubtype("BULK")
                .build();

        assertThrows(IllegalArgumentException.class, () -> validationService.validate(definition, command));
    }

    @Test
    void validate_withValidConfiguration_shouldPass() {
        LinkDefinitionEntity definition = baseDefinition();
        definition.setAllowedEntityTypes(Set.of("WORKFLOW"));

        CreateAssetLinkCommand command = CreateAssetLinkCommand.builder()
                .assetId("A1")
                .entityType("WORKFLOW")
                .entityId("WF-1")
                .linkSubtype("BULK")
                .build();

        assertDoesNotThrow(() -> validationService.validate(definition, command));
    }

    @Test
    void validateCardinality_oneToOneExisting_shouldThrow() {
        LinkDefinitionEntity definition = baseDefinition();
        definition.setCardinality(LinkCardinality.ONE_TO_ONE);
        definition.setAllowedEntityTypes(Set.of("WORKFLOW"));

        when(assetLinkRepository.countByAssetIdAndLinkCodeAndLinkSubtypeAndActiveIsTrueAndDeletedIsFalse(anyString(), anyString(), anyString()))
                .thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> validationService.validate(definition, "A1", "WORKFLOW", "WF-1", "BULK", true));
    }

    @Test
    void validateCardinality_manyToOneAssetActive_shouldThrow() {
        LinkDefinitionEntity definition = baseDefinition();
        definition.setCardinality(LinkCardinality.MANY_TO_ONE);
        definition.setAllowedEntityTypes(Set.of("WORKFLOW"));

        when(assetLinkRepository.countByAssetIdAndLinkCodeAndLinkSubtypeAndActiveIsTrueAndDeletedIsFalse("A1", "WORKFLOW", "BULK"))
                .thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> validationService.validate(definition, "A1", "WORKFLOW", "WF-1", "BULK", true));
    }

    @Test
    void validateCardinality_oneToMany_noEntityConflict_shouldPass() {
        LinkDefinitionEntity definition = baseDefinition();
        definition.setCardinality(LinkCardinality.ONE_TO_MANY);
        definition.setAllowedEntityTypes(Set.of("WORKFLOW"));

        when(assetLinkRepository.countByAssetIdAndLinkCodeAndLinkSubtypeAndActiveIsTrueAndDeletedIsFalse(anyString(), anyString(), anyString()))
                .thenReturn(0L);
        when(assetLinkRepository.countByEntityTypeAndEntityIdAndLinkCodeAndLinkSubtypeAndActiveIsTrueAndDeletedIsFalse(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(0L);

        assertDoesNotThrow(() -> validationService.validate(definition, "A1", "WORKFLOW", "WF-1", "BULK", true));
    }

    @Test
    void validateCardinality_withInactiveCommand_shouldSkipChecks() {
        LinkDefinitionEntity definition = baseDefinition();
        definition.setAllowedEntityTypes(Set.of("WORKFLOW"));

        CreateAssetLinkCommand command = CreateAssetLinkCommand.builder()
                .assetId("A1")
                .entityType("WORKFLOW")
                .entityId("WF-1")
                .linkSubtype("BULK")
                .active(false)
                .build();

        assertDoesNotThrow(() -> validationService.validate(definition, command));
        verifyNoInteractions(assetLinkRepository);
    }

    private LinkDefinitionEntity baseDefinition() {
        return LinkDefinitionEntity.builder()
                .code("WORKFLOW")
                .entityType("WORKFLOW")
                .cardinality(LinkCardinality.ONE_TO_ONE)
                .enabled(true)
                .build();
    }

}

