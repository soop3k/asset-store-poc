package com.db.assetstore.service;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.service.cmd.CommandResult;
import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.cmd.DeleteAssetCommand;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.infra.jpa.AttributeEntity;
import com.db.assetstore.infra.config.JsonMapperProvider;
import com.db.assetstore.infra.jpa.CommandLogEntity;
import com.db.assetstore.infra.mapper.AssetMapper;
import com.db.assetstore.infra.mapper.AttributeMapper;
import com.db.assetstore.infra.repository.AssetRepository;
import com.db.assetstore.infra.repository.AttributeRepository;
import com.db.assetstore.infra.repository.CommandLogRepository;
import com.db.assetstore.infra.service.AssetCommandServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AssetCommandServiceImplTest {

    AssetMapper assetMapper;
    AttributeMapper attributeMapper;
    AssetRepository assetRepo;
    AttributeRepository attributeRepo;
    CommandLogRepository commandLogRepository;
    ObjectMapper objectMapper;

    AssetCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        assetMapper = mock(AssetMapper.class);
        attributeMapper = mock(AttributeMapper.class, Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS));
        assetRepo = mock(AssetRepository.class);
        attributeRepo = mock(AttributeRepository.class);
        commandLogRepository = mock(CommandLogRepository.class);
        objectMapper = new JsonMapperProvider().objectMapper();
        when(commandLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        service = new AssetCommandServiceImpl(assetMapper, attributeMapper, assetRepo, attributeRepo, commandLogRepository, objectMapper);

        // Default behavior for save to echo the entity
        when(assetRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(attributeRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void create_whenEntityAlreadyContainsAttributes_savesDirectlyAndReturnsId() {
        // given command with attributes
        CreateAssetCommand cmd = CreateAssetCommand.builder()
                .id("a-1").type(AssetType.CRE)
                .attribute(new AVString("city", "Warsaw"))
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

        // when
        String id = service.create(cmd);

        // then
        assertEquals("a-1", id);
        verify(assetRepo, times(1)).save(entity);
        verifyNoInteractions(attributeRepo);

        ArgumentCaptor<CommandLogEntity> logCaptor = ArgumentCaptor.forClass(CommandLogEntity.class);
        verify(commandLogRepository).save(logCaptor.capture());
        assertEquals("CreateAssetCommand", logCaptor.getValue().getCommandType());
        assertEquals("a-1", logCaptor.getValue().getAssetId());
        assertEquals("test", logCaptor.getValue().getExecutedBy());
    }

    @Test
    void create_whenEntityHasNoAttributes_insertsAllOnCreateAndSaves() {
        // given command with a single attribute
        CreateAssetCommand cmd = CreateAssetCommand.builder()
                .id("a-2").type(AssetType.CRE)
                .attribute(new AVString("city", "Gdansk"))
                .executedBy("creator")
                .build();

        // mapper returns entity with empty list -> triggers insertAllOnCreate
        AssetEntity entity = AssetEntity.builder().id("a-2").type(AssetType.CRE).attributes(new java.util.ArrayList<>()).build();
        when(assetMapper.toEntity(any())).thenReturn(entity);

        // attributeMapper.toEntity will be called and we let real default method build AttributeEntity
        // when
        String id = service.create(cmd);

        // then
        assertEquals("a-2", id);
        // One save of parent entity after children have been attached
        verify(assetRepo, times(1)).save(entity);
        // AttributeRepository is not used in insertAllOnCreate path
        verify(attributeRepo, never()).save(any());
        // The attribute should have been attached to the entity
        assertFalse(entity.getAttributes().isEmpty());
        assertEquals("city", entity.getAttributes().get(0).getName());

        ArgumentCaptor<CommandLogEntity> logCaptor = ArgumentCaptor.forClass(CommandLogEntity.class);
        verify(commandLogRepository).save(logCaptor.capture());
        assertEquals("CreateAssetCommand", logCaptor.getValue().getCommandType());
        assertEquals("a-2", logCaptor.getValue().getAssetId());
        assertEquals("creator", logCaptor.getValue().getExecutedBy());
    }

    @Test
    void execute_withCreateCommand_dispatchesThroughVisitorAndRecordsLog() {
        CreateAssetCommand cmd = CreateAssetCommand.builder()
                .id("exec-1")
                .type(AssetType.CRE)
                .attribute(new AVString("city", "Poznan"))
                .executedBy("tester")
                .build();

        AssetEntity entity = AssetEntity.builder().id("exec-1").type(AssetType.CRE).attributes(new java.util.ArrayList<>()).build();
        when(assetMapper.toEntity(any())).thenReturn(entity);

        CommandResult<String> result = service.execute(cmd);

        assertEquals("exec-1", result.result());
        assertEquals("exec-1", result.assetId());
        assertEquals("tester", result.executedBy());
        verify(assetRepo).save(entity);

        ArgumentCaptor<CommandLogEntity> logCaptor = ArgumentCaptor.forClass(CommandLogEntity.class);
        verify(commandLogRepository).save(logCaptor.capture());
        assertEquals("CreateAssetCommand", logCaptor.getValue().getCommandType());
        assertEquals("exec-1", logCaptor.getValue().getAssetId());
        assertEquals("tester", logCaptor.getValue().getExecutedBy());
    }

    @Test
    void update_commonFieldsOnly_persistsChanges() {
        AssetEntity entity = AssetEntity.builder().id("a-3").type(AssetType.CRE).status("ACTIVE").build();
        when(assetRepo.findByIdAndDeleted("a-3", 0)).thenReturn(Optional.of(entity));

        PatchAssetCommand cmd = PatchAssetCommand.builder()
                .assetId("a-3")
                .status("INACTIVE")
                .executedBy("updater")
                .build();

        service.update(cmd);

        assertEquals("INACTIVE", entity.getStatus());
        verify(assetRepo, times(1)).save(entity);
        verify(attributeRepo, never()).save(any());

        ArgumentCaptor<CommandLogEntity> logCaptor = ArgumentCaptor.forClass(CommandLogEntity.class);
        verify(commandLogRepository).save(logCaptor.capture());
        assertEquals("PatchAssetCommand", logCaptor.getValue().getCommandType());
        assertEquals("a-3", logCaptor.getValue().getAssetId());
        assertEquals("updater", logCaptor.getValue().getExecutedBy());
    }

    @Test
    void update_attributeChanged_updatesAttributeAndSavesIt() {
        // existing entity with attribute city="Gdansk"
        AssetEntity parent = AssetEntity.builder().id("a-4").type(AssetType.CRE).attributes(new java.util.ArrayList<>()).build();
        AttributeEntity existing = new AttributeEntity(parent, "city", "Gdansk", Instant.now());
        parent.getAttributes().add(existing);
        when(assetRepo.findByIdAndDeleted("a-4", 0)).thenReturn(Optional.of(parent));

        // incoming patch with city="Warsaw"
        PatchAssetCommand cmd = PatchAssetCommand.builder()
                .assetId("a-4")
                .attributes(List.of(new AVString("city", "Warsaw")))
                .executedBy("modifier")
                .build();

        service.update(cmd);

        // verify changed and saved
        assertEquals("Warsaw", parent.getAttributes().get(0).getValueStr());
        verify(attributeRepo, times(1)).save(parent.getAttributes().get(0));

        ArgumentCaptor<CommandLogEntity> logCaptor = ArgumentCaptor.forClass(CommandLogEntity.class);
        verify(commandLogRepository).save(logCaptor.capture());
        assertEquals("PatchAssetCommand", logCaptor.getValue().getCommandType());
        assertEquals("a-4", logCaptor.getValue().getAssetId());
        assertEquals("modifier", logCaptor.getValue().getExecutedBy());
    }

    @Test
    void update_attributeSameValue_doesNotSaveAttribute() {
        AssetEntity parent = AssetEntity.builder().id("a-5").type(AssetType.CRE).attributes(new java.util.ArrayList<>()).build();
        AttributeEntity existing = new AttributeEntity(parent, "city", "Warsaw", Instant.now());
        parent.getAttributes().add(existing);
        when(assetRepo.findByIdAndDeleted("a-5", 0)).thenReturn(Optional.of(parent));

        PatchAssetCommand cmd = PatchAssetCommand.builder()
                .assetId("a-5")
                .attributes(List.of(new AVString("city", "Warsaw")))
                .executedBy("modifier")
                .build();

        service.update(cmd);

        // value remains and attributeRepo.save not called
        assertEquals("Warsaw", parent.getAttributes().get(0).getValueStr());
        verify(attributeRepo, never()).save(any());

        ArgumentCaptor<CommandLogEntity> logCaptor = ArgumentCaptor.forClass(CommandLogEntity.class);
        verify(commandLogRepository).save(logCaptor.capture());
        assertEquals("PatchAssetCommand", logCaptor.getValue().getCommandType());
        assertEquals("a-5", logCaptor.getValue().getAssetId());
        assertEquals("modifier", logCaptor.getValue().getExecutedBy());
    }

    @Test
    void delete_marksEntityDeletedAndSaves() {
        AssetEntity entity = AssetEntity.builder().id("a-6").type(AssetType.CRE).deleted(0).build();
        when(assetRepo.findByIdAndDeleted("a-6", 0)).thenReturn(Optional.of(entity));

        service.delete(DeleteAssetCommand.builder().assetId("a-6").executedBy("deleter").build());

        assertEquals(1, entity.getDeleted());
        verify(assetRepo, times(1)).save(entity);

        ArgumentCaptor<CommandLogEntity> logCaptor = ArgumentCaptor.forClass(CommandLogEntity.class);
        verify(commandLogRepository).save(logCaptor.capture());
        assertEquals("DeleteAssetCommand", logCaptor.getValue().getCommandType());
        assertEquals("a-6", logCaptor.getValue().getAssetId());
        assertEquals("deleter", logCaptor.getValue().getExecutedBy());
    }
}
