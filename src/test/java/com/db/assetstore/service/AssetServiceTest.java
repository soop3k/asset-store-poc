package com.db.assetstore.service;

import com.db.assetstore.domain.model.asset.AssetPatch;
import com.db.assetstore.domain.model.asset.AssetType;
import com.db.assetstore.domain.service.cmd.CommandResult;
import com.db.assetstore.domain.service.asset.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.asset.cmd.DeleteAssetCommand;
import com.db.assetstore.domain.service.asset.cmd.PatchAssetCommand;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.infra.jpa.AttributeEntity;
import com.db.assetstore.infra.mapper.AssetCommandMapper;
import com.db.assetstore.infra.mapper.AssetHistoryMapper;
import com.db.assetstore.infra.mapper.AssetMapper;
import com.db.assetstore.infra.mapper.AttributeMapper;
import com.db.assetstore.infra.repository.AssetHistoryRepository;
import com.db.assetstore.infra.repository.AssetRepository;
import com.db.assetstore.infra.repository.AttributeRepository;
import com.db.assetstore.infra.service.AssetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AssetServiceTest {

    AssetMapper assetMapper;
    AttributeMapper attributeMapper;
    AssetRepository assetRepo;
    AttributeRepository attributeRepo;
    AssetHistoryRepository assetHistoryRepo;
    AssetHistoryMapper assetHistoryMapper;
    AssetCommandMapper assetCommandMapper;

    AssetService service;

    @BeforeEach
    void setUp() {
        assetMapper = mock(AssetMapper.class);
        attributeMapper = mock(AttributeMapper.class, Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS));
        assetRepo = mock(AssetRepository.class);
        attributeRepo = mock(AttributeRepository.class);
        assetHistoryRepo = mock(AssetHistoryRepository.class);
        assetHistoryMapper = mock(AssetHistoryMapper.class);
        assetCommandMapper = mock(AssetCommandMapper.class);

        service = new AssetService(
                assetMapper,
                assetCommandMapper,
                attributeMapper,
                assetRepo,
                attributeRepo,
                assetHistoryRepo,
                assetHistoryMapper);

        when(assetRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(attributeRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(assetHistoryRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(assetHistoryMapper.toEntity(any(), any())).thenReturn(new com.db.assetstore.infra.jpa.AssetHistoryEntity());
        when(assetCommandMapper.fromCreateCommand(any(), any(), any(), any())).thenAnswer(inv -> {
            CreateAssetCommand cmd = inv.getArgument(0);
            String assetId = inv.getArgument(1);
            java.time.Instant createdAt = inv.getArgument(2);
            java.time.Instant modifiedAt = inv.getArgument(3);
            com.db.assetstore.domain.model.asset.Asset asset = com.db.assetstore.domain.model.asset.Asset.builder()
                    .id(assetId)
                    .type(cmd.type())
                    .createdAt(createdAt)
                    .status(cmd.status())
                    .subtype(cmd.subtype())
                    .notionalAmount(cmd.notionalAmount())
                    .year(cmd.year())
                    .description(cmd.description())
                    .currency(cmd.currency())
                    .createdBy(cmd.executedBy())
                    .modifiedBy(cmd.executedBy())
                    .modifiedAt(modifiedAt)
                    .build();
            asset.setAttributes(cmd.attributes());
            return asset;
        });
        when(assetCommandMapper.toPatch(any())).thenAnswer(inv -> {
            PatchAssetCommand cmd = inv.getArgument(0);
            return AssetPatch.builder()
                    .status(cmd.status())
                    .subtype(cmd.subtype())
                    .notionalAmount(cmd.notionalAmount())
                    .year(cmd.year())
                    .description(cmd.description())
                    .currency(cmd.currency())
                    .attributes(cmd.attributes())
                    .build();
        });
    }

    @Test
    void whenEntityAlreadyContainsAttributes() {
        CreateAssetCommand cmd = CreateAssetCommand.builder()
                .id("a-1").type(AssetType.CRE)
                .attributes(List.of(new AVString("city", "Warsaw")))
                .status("ACTIVE")
                .executedBy("test")
                .requestTime(Instant.now())
                .build();

        AssetEntity parent = AssetEntity.builder().id("a-1").type(AssetType.CRE).build();
        AttributeEntity attr = new AttributeEntity(parent, "city", "Warsaw", Instant.now());
        attr.setName("city");
        attr.setValueStr("Warsaw");
        AssetEntity entity = AssetEntity.builder().id("a-1").type(AssetType.CRE).attributes(List.of(attr)).build();
        when(assetMapper.toEntity(any())).thenReturn(entity);

        CommandResult<String> result = service.create(cmd);

        assertEquals("a-1", result.result());
        assertEquals("a-1", result.assetId());
        verify(assetRepo, times(1)).save(entity);
        verifyNoInteractions(attributeRepo);
        verify(assetHistoryRepo, times(1)).save(any());
    }

    @Test
    void whenEntityHasNoAttributes() {
        CreateAssetCommand cmd = CreateAssetCommand.builder()
                .id("a-2").type(AssetType.CRE)
                .attributes(List.of(new AVString("city", "Gdansk")))
                .executedBy("creator")
                .build();

        AssetEntity entity = AssetEntity.builder()
                .id("a-2")
                .type(AssetType.CRE)
                .attributes(new java.util.ArrayList<>())
                .build();
        when(assetMapper.toEntity(any())).thenReturn(entity);

        CommandResult<String> result = service.create(cmd);

        assertEquals("a-2", result.result());
        assertEquals("a-2", result.assetId());
        verify(assetRepo, times(1)).save(entity);
        verify(attributeRepo, never()).save(any());
        assertFalse(entity.getAttributes().isEmpty());
        assertEquals("city", entity.getAttributes().get(0).getName());
        verify(assetHistoryRepo, times(1)).save(any());
    }

    @Test
    void patchCommonFieldsOnly() {
        AssetEntity entity = AssetEntity.builder().id("a-3").type(AssetType.CRE).status("ACTIVE").build();
        when(assetRepo.findByIdAndDeleted("a-3", 0)).thenReturn(Optional.of(entity));

        PatchAssetCommand cmd = PatchAssetCommand.builder()
                .assetId("a-3")
                .status("INACTIVE")
                .executedBy("updater")
                .build();

        CommandResult<Void> result = service.patch(cmd);

        assertTrue(result.success());
        assertEquals("a-3", result.assetId());
        assertEquals("INACTIVE", entity.getStatus());
        verify(assetRepo, times(1)).save(entity);
        verify(attributeRepo, never()).save(any());
        verify(assetHistoryRepo, times(1)).save(any());
    }

    @Test
    void updatesAttributeAndSavesIt() {
        AssetEntity parent = AssetEntity.builder().id("a-4").type(AssetType.CRE).attributes(new java.util.ArrayList<>()).build();
        AttributeEntity existing = new AttributeEntity(parent, "city", "Gdansk", Instant.now());
        parent.getAttributes().add(existing);
        when(assetRepo.findByIdAndDeleted("a-4", 0)).thenReturn(Optional.of(parent));

        PatchAssetCommand cmd = PatchAssetCommand.builder()
                .assetId("a-4")
                .attributes(List.of(new AVString("city", "Warsaw")))
                .executedBy("modifier")
                .build();

        service.patch(cmd);

        assertEquals("Warsaw", parent.getAttributes().get(0).getValueStr());
        verify(attributeRepo, times(1)).save(parent.getAttributes().get(0));
        verify(assetHistoryRepo, times(1)).save(any());
    }

    @Test
    void doesNotSaveAttribute() {
        AssetEntity parent = AssetEntity.builder().id("a-5").type(AssetType.CRE).attributes(new java.util.ArrayList<>()).build();
        AttributeEntity existing = new AttributeEntity(parent, "city", "Warsaw", Instant.now());
        parent.getAttributes().add(existing);
        when(assetRepo.findByIdAndDeleted("a-5", 0)).thenReturn(Optional.of(parent));

        PatchAssetCommand cmd = PatchAssetCommand.builder()
                .assetId("a-5")
                .attributes(List.of(new AVString("city", "Warsaw")))
                .executedBy("modifier")
                .build();

        service.patch(cmd);

        assertEquals("Warsaw", parent.getAttributes().get(0).getValueStr());
        verify(attributeRepo, never()).save(any());
        verify(assetHistoryRepo, times(1)).save(any());
    }

    @Test
    void marksEntityDeletedAndSaves() {
        AssetEntity entity = AssetEntity.builder().id("a-6").type(AssetType.CRE).deleted(0).build();
        when(assetRepo.findByIdAndDeleted("a-6", 0)).thenReturn(Optional.of(entity));

        CommandResult<Void> result = service.delete(DeleteAssetCommand.builder().assetId("a-6").executedBy("deleter").build());

        assertTrue(result.success());
        assertEquals("a-6", result.assetId());
        assertEquals(1, entity.getDeleted());
        verify(assetRepo, times(1)).save(entity);
        verify(assetHistoryRepo, times(1)).save(any());
    }
}
