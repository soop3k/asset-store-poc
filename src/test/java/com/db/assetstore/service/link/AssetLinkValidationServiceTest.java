package com.db.assetstore.service.link;

import com.db.assetstore.domain.model.link.LinkCardinality;
import com.db.assetstore.domain.service.link.cmd.CreateAssetLinkCommand;
import com.db.assetstore.infra.jpa.link.LinkDefinitionEntity;
import com.db.assetstore.infra.jpa.link.LinkSubtypeDefinitionEntity;
import com.db.assetstore.infra.jpa.link.LinkSubtypeDefinitionId;
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
    void validateDefinition_whenDisabled_shouldThrow() {
        LinkDefinitionEntity definition = baseDefinition().toBuilder().enabled(false).build();
        definition.setSubtypes(Set.of(subtype(definition, "BULK")));

        assertThrows(IllegalStateException.class, () -> validationService.validateDefinition(definition, "WORKFLOW", "WF-1", "BULK"));
    }

    @Test
    void validateDefinition_whenSubtypeUnknown_shouldThrow() {
        LinkDefinitionEntity definition = baseDefinition();
        definition.setSubtypes(Set.of(subtype(definition, "BULK")));

        assertThrows(IllegalArgumentException.class, () -> validationService.validateDefinition(definition, "WORKFLOW", "WF-1", "MONITORING"));
    }

    @Test
    void validateDefinition_withValidConfiguration_shouldPass() {
        LinkDefinitionEntity definition = baseDefinition();
        definition.setSubtypes(Set.of(subtype(definition, "BULK")));

        assertDoesNotThrow(() -> validationService.validateDefinition(definition, "WORKFLOW", "WF-1", "BULK"));
    }

    @Test
    void validateCardinality_oneToOneExisting_shouldThrow() {
        LinkDefinitionEntity definition = baseDefinition();
        definition.setCardinality(LinkCardinality.ONE_TO_ONE);
        definition.setSubtypes(Set.of(subtype(definition, "BULK")));

        when(assetLinkRepository.countByAssetIdAndLinkCodeAndLinkSubtypeAndActiveIsTrueAndDeletedIsFalse(anyString(), anyString(), anyString()))
                .thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> validationService.validateCardinality(definition, "A1", "WORKFLOW", "WF-1", "BULK"));
    }

    @Test
    void validateCardinality_manyToOneAssetActive_shouldThrow() {
        LinkDefinitionEntity definition = baseDefinition();
        definition.setCardinality(LinkCardinality.MANY_TO_ONE);
        definition.setSubtypes(Set.of(subtype(definition, "BULK")));

        when(assetLinkRepository.countByAssetIdAndLinkCodeAndLinkSubtypeAndActiveIsTrueAndDeletedIsFalse("A1", "WORKFLOW", "BULK"))
                .thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> validationService.validateCardinality(definition, "A1", "WORKFLOW", "WF-1", "BULK"));
    }

    @Test
    void validateCardinality_oneToMany_noEntityConflict_shouldPass() {
        LinkDefinitionEntity definition = baseDefinition();
        definition.setCardinality(LinkCardinality.ONE_TO_MANY);
        definition.setSubtypes(Set.of(subtype(definition, "BULK")));

        when(assetLinkRepository.countByAssetIdAndLinkCodeAndLinkSubtypeAndActiveIsTrueAndDeletedIsFalse(anyString(), anyString(), anyString()))
                .thenReturn(0L);
        when(assetLinkRepository.countByEntityTypeAndEntityIdAndLinkCodeAndLinkSubtypeAndActiveIsTrueAndDeletedIsFalse(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(0L);

        assertDoesNotThrow(() -> validationService.validateCardinality(definition, "A1", "WORKFLOW", "WF-1", "BULK"));
    }

    @Test
    void validateDefinition_withCommandDelegates() {
        LinkDefinitionEntity definition = baseDefinition();
        definition.setSubtypes(Set.of(subtype(definition, "BULK")));

        CreateAssetLinkCommand command = CreateAssetLinkCommand.builder()
                .assetId("A1")
                .entityType("WORKFLOW")
                .entityId("WF-1")
                .linkSubtype("BULK")
                .build();

        assertDoesNotThrow(() -> validationService.validateDefinition(definition, command));
    }

    @Test
    void validateCardinality_withInactiveCommand_shouldSkipChecks() {
        LinkDefinitionEntity definition = baseDefinition();
        definition.setSubtypes(Set.of(subtype(definition, "BULK")));

        CreateAssetLinkCommand command = CreateAssetLinkCommand.builder()
                .assetId("A1")
                .entityType("WORKFLOW")
                .entityId("WF-1")
                .linkSubtype("BULK")
                .active(false)
                .build();

        assertDoesNotThrow(() -> validationService.validateCardinality(definition, command));
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

    private LinkSubtypeDefinitionEntity subtype(LinkDefinitionEntity definition, String subtype) {
        return LinkSubtypeDefinitionEntity.builder()
                .id(new LinkSubtypeDefinitionId(definition.getCode(), subtype))
                .definition(definition)
                .build();
    }
}

