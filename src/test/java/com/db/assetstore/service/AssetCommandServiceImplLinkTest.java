package com.db.assetstore.service;

import com.db.assetstore.domain.model.link.LinkCardinality;
import com.db.assetstore.domain.service.link.cmd.CreateAssetLinkCommand;
import com.db.assetstore.domain.service.link.cmd.DeleteAssetLinkCommand;
import com.db.assetstore.domain.service.link.cmd.PatchAssetLinkCommand;
import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.infra.jpa.link.AssetLinkEntity;
import com.db.assetstore.infra.jpa.link.LinkDefinitionEntity;
import com.db.assetstore.infra.jpa.link.LinkSubtypeDefinitionEntity;
import com.db.assetstore.infra.jpa.link.LinkSubtypeDefinitionId;
import com.db.assetstore.infra.mapper.AssetMapper;
import com.db.assetstore.infra.mapper.AttributeMapper;
import com.db.assetstore.infra.repository.AssetRepository;
import com.db.assetstore.infra.repository.AttributeRepository;
import com.db.assetstore.infra.repository.link.AssetLinkRepository;
import com.db.assetstore.infra.repository.link.LinkDefinitionRepository;
import com.db.assetstore.infra.service.AssetCommandServiceImpl;
import com.db.assetstore.infra.service.link.AssetLinkValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AssetCommandServiceImplLinkTest {

    AssetRepository assetRepository;
    AttributeRepository attributeRepository;
    AssetLinkRepository assetLinkRepository;
    LinkDefinitionRepository linkDefinitionRepository;
    AssetMapper assetMapper;
    AttributeMapper attributeMapper;
    AssetLinkValidationService assetLinkValidationService;

    AssetCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        assetRepository = mock(AssetRepository.class);
        attributeRepository = mock(AttributeRepository.class);
        assetLinkRepository = mock(AssetLinkRepository.class);
        linkDefinitionRepository = mock(LinkDefinitionRepository.class);
        assetMapper = mock(AssetMapper.class);
        attributeMapper = mock(AttributeMapper.class);
        assetLinkValidationService = new AssetLinkValidationService(assetLinkRepository);
        service = new AssetCommandServiceImpl(assetMapper, attributeMapper, assetRepository, attributeRepository, assetLinkRepository, linkDefinitionRepository, assetLinkValidationService);
    }

    @Test
    void create_whenDefinitionDisabled_shouldThrow() {
        CreateAssetLinkCommand command = CreateAssetLinkCommand.builder()
                .assetId("A1")
                .linkCode("WORKFLOW")
                .linkSubtype("BULK")
                .entityType("WORKFLOW")
                .entityId("WF-1")
                .build();

        AssetEntity asset = AssetEntity.builder().id("A1").build();
        when(assetRepository.findByIdAndDeleted("A1", 0)).thenReturn(Optional.of(asset));
        LinkDefinitionEntity definition = LinkDefinitionEntity.builder()
                .code("WORKFLOW")
                .entityType("WORKFLOW")
                .cardinality(LinkCardinality.ONE_TO_ONE)
                .enabled(false)
                .build();
        definition.setAllowedEntityTypes(Set.of(buildEntityType(definition, "WORKFLOW")));
        when(linkDefinitionRepository.findByCodeIgnoreCase("WORKFLOW")).thenReturn(Optional.of(definition));

        assertThrows(IllegalStateException.class, () -> service.createLink(command));
    }

    @Test
    void create_oneToOneExistingLink_shouldThrow() {
        CreateAssetLinkCommand command = CreateAssetLinkCommand.builder()
                .assetId("A1")
                .linkCode("WORKFLOW")
                .linkSubtype("BULK")
                .entityType("WORKFLOW")
                .entityId("WF-1")
                .build();

        AssetEntity asset = AssetEntity.builder().id("A1").build();
        when(assetRepository.findByIdAndDeleted("A1", 0)).thenReturn(Optional.of(asset));
        LinkDefinitionEntity definition = LinkDefinitionEntity.builder()
                .code("WORKFLOW")
                .entityType("WORKFLOW")
                .cardinality(LinkCardinality.ONE_TO_ONE)
                .enabled(true)
                .build();
        definition.setAllowedEntityTypes(Set.of(buildEntityType(definition, "WORKFLOW")));
        when(linkDefinitionRepository.findByCodeIgnoreCase("WORKFLOW")).thenReturn(Optional.of(definition));
        when(assetLinkRepository.countByAssetIdAndLinkCodeAndLinkSubtypeAndActiveIsTrueAndDeletedIsFalse("A1", "WORKFLOW", "BULK"))
                .thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> service.createLink(command));
    }

    @Test
    void create_success_savesLink() {
        CreateAssetLinkCommand command = CreateAssetLinkCommand.builder()
                .assetId("A1")
                .linkCode("WORKFLOW")
                .linkSubtype("BULK")
                .entityType("WORKFLOW")
                .entityId("WF-1")
                .requestedBy("tester")
                .requestTime(Instant.parse("2024-01-01T00:00:00Z"))
                .build();

        AssetEntity asset = AssetEntity.builder().id("A1").build();
        when(assetRepository.findByIdAndDeleted("A1", 0)).thenReturn(Optional.of(asset));
        LinkDefinitionEntity definition = LinkDefinitionEntity.builder()
                .code("WORKFLOW")
                .entityType("WORKFLOW")
                .cardinality(LinkCardinality.MANY_TO_ONE)
                .enabled(true)
                .build();
        definition.setAllowedEntityTypes(Set.of(buildEntityType(definition, "WORKFLOW")));
        when(linkDefinitionRepository.findByCodeIgnoreCase("WORKFLOW")).thenReturn(Optional.of(definition));
        when(assetLinkRepository.existsByAssetIdAndEntityTypeAndEntityIdAndLinkCodeAndLinkSubtypeAndDeletedIsFalse(any(), any(), any(), any(), any()))
                .thenReturn(false);

        ArgumentCaptor<AssetLinkEntity> entityCaptor = ArgumentCaptor.forClass(AssetLinkEntity.class);

        String id = service.createLink(command);

        assertNotNull(id);
        verify(assetLinkRepository, times(1)).save(entityCaptor.capture());
        assertEquals(id, entityCaptor.getValue().getId());
    }

    @Test
    void delete_marksLinkInactive() {
        DeleteAssetLinkCommand command = DeleteAssetLinkCommand.builder()
                .assetId("A1")
                .linkId("L1")
                .requestedBy("tester")
                .requestTime(Instant.parse("2024-01-01T00:00:00Z"))
                .build();

        AssetLinkEntity entity = AssetLinkEntity.builder()
                .id("L1")
                .assetId("A1")
                .active(true)
                .deleted(false)
                .build();

        when(assetLinkRepository.findByIdAndDeletedIsFalse("L1")).thenReturn(Optional.of(entity));

        service.deleteLink(command);

        assertFalse(entity.isActive());
        assertTrue(entity.isDeleted());
        verify(assetLinkRepository, times(1)).save(entity);
    }

    @Test
    void patch_reactivateLink_validatesCardinality() {
        PatchAssetLinkCommand command = PatchAssetLinkCommand.builder()
                .assetId("A1")
                .linkId("L1")
                .active(true)
                .requestedBy("tester")
                .requestTime(Instant.parse("2024-02-01T00:00:00Z"))
                .build();

        AssetLinkEntity entity = AssetLinkEntity.builder()
                .id("L1")
                .assetId("A1")
                .linkCode("WORKFLOW")
                .linkSubtype("BULK")
                .entityType("WORKFLOW")
                .entityId("WF-1")
                .active(false)
                .deleted(false)
                .build();

        LinkDefinitionEntity definition = LinkDefinitionEntity.builder()
                .code("WORKFLOW")
                .entityType("WORKFLOW")
                .cardinality(LinkCardinality.ONE_TO_ONE)
                .enabled(true)
                .build();
        definition.setAllowedEntityTypes(Set.of(buildEntityType(definition, "WORKFLOW")));

        when(assetLinkRepository.findByIdAndDeletedIsFalse("L1")).thenReturn(Optional.of(entity));
        when(linkDefinitionRepository.findByCodeIgnoreCase("WORKFLOW")).thenReturn(Optional.of(definition));
        when(assetLinkRepository.countByAssetIdAndLinkCodeAndLinkSubtypeAndActiveIsTrueAndDeletedIsFalse("A1", "WORKFLOW", "BULK"))
                .thenReturn(0L);
        when(assetLinkRepository.countByEntityTypeAndEntityIdAndLinkCodeAndLinkSubtypeAndActiveIsTrueAndDeletedIsFalse("WORKFLOW", "WF-1", "WORKFLOW", "BULK"))
                .thenReturn(0L);

        service.patchLink(command);

        assertTrue(entity.isActive());
        assertFalse(entity.isDeleted());
        verify(assetLinkRepository).save(entity);
    }

    @Test
    void patch_updatesValidityDates() {
        Instant newFrom = Instant.parse("2024-03-01T00:00:00Z");
        Instant newTo = Instant.parse("2024-03-31T00:00:00Z");
        PatchAssetLinkCommand command = PatchAssetLinkCommand.builder()
                .assetId("A1")
                .linkId("L1")
                .validFrom(newFrom)
                .validTo(newTo)
                .requestedBy("tester")
                .requestTime(Instant.parse("2024-03-02T00:00:00Z"))
                .build();

        AssetLinkEntity entity = AssetLinkEntity.builder()
                .id("L1")
                .assetId("A1")
                .active(true)
                .deleted(false)
                .build();

        when(assetLinkRepository.findByIdAndDeletedIsFalse("L1")).thenReturn(Optional.of(entity));

        service.patchLink(command);

        assertEquals(newFrom, entity.getValidFrom());
        assertEquals(newTo, entity.getValidTo());
        verify(assetLinkRepository).save(entity);
    }

    private LinkSubtypeDefinitionEntity buildEntityType(LinkDefinitionEntity parent, String entityType) {
        return LinkSubtypeDefinitionEntity.builder()
                .id(new LinkSubtypeDefinitionId(parent.getCode(), entityType))
                .definition(parent)
                .build();
    }
}
