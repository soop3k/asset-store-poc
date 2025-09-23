package com.db.assetstore.infra.service;

import com.db.assetstore.domain.service.cmd.AssetCommand;
import com.db.assetstore.domain.service.cmd.CommandResult;
import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.cmd.DeleteAssetCommand;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;
import com.db.assetstore.domain.service.cmd.AssetCommandVisitor;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.AssetPatch;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.service.AssetCommandService;
import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.infra.jpa.AttributeEntity;
import com.db.assetstore.infra.jpa.CommandLogEntity;
import com.db.assetstore.infra.mapper.AssetMapper;
import com.db.assetstore.infra.mapper.AttributeMapper;
import com.db.assetstore.infra.repository.AssetRepository;
import com.db.assetstore.infra.repository.AttributeRepository;
import com.db.assetstore.infra.repository.CommandLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Command-side implementation operating on Asset entities.
 * Implements creation, updates (common fields and attributes), and soft delete.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetCommandServiceImpl implements AssetCommandService, AssetCommandVisitor {

    private final AssetMapper assetMapper;
    private final AttributeMapper attributeMapper;
    private final AssetRepository assetRepo;
    private final AttributeRepository attributeRepo;
    private final CommandLogRepository commandLogRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public String create(CreateAssetCommand command) {
        return execute(command).result();
    }

    @Override
    @Transactional
    public void update(PatchAssetCommand command) {
        execute(command);
    }

    @Override
    @Transactional
    public void delete(DeleteAssetCommand command) {
        execute(command);
    }

    @Override
    @Transactional
    public <R> CommandResult<R> execute(AssetCommand<R> command) {
        Objects.requireNonNull(command, "command");
        CommandResult<R> result = command.accept(this);
        recordCommand(result.assetId(), command);
        return result;
    }

    @Override
    public CommandResult<String> visit(CreateAssetCommand command) {
        Objects.requireNonNull(command, "command");
        String assetId = resolveAssetId(command);
        Asset asset = Asset.builder()
                .id(assetId)
                .type(command.type())
                .createdAt(Instant.now())
                .status(command.status())
                .subtype(command.subtype())
                .notionalAmount(command.notionalAmount())
                .year(command.year())
                .description(command.description())
                .currency(command.currency())
                .createdBy(command.createdBy())
                .modifiedBy(command.createdBy())
                .modifiedAt(Instant.now())
                .build();
        asset.setAttributes(command.attributes());
        String persistedId = persistAsset(asset);
        return new CommandResult<>(persistedId, persistedId);
    }

    @Override
    public CommandResult<Void> visit(PatchAssetCommand command) {
        Objects.requireNonNull(command, "command");
        String assetId = Objects.requireNonNull(command.assetId(), "assetId");
        AssetPatch patch = AssetPatch.builder()
                .status(command.status())
                .subtype(command.subtype())
                .notionalAmount(command.notionalAmount())
                .year(command.year())
                .description(command.description())
                .currency(command.currency())
                .attributes(command.attributes())
                .build();
        applyPatch(assetId, patch);
        return CommandResult.noResult(assetId);
    }

    @Override
    public CommandResult<Void> visit(DeleteAssetCommand command) {
        Objects.requireNonNull(command, "command");
        String assetId = Objects.requireNonNull(command.assetId(), "assetId");
        deleteAsset(assetId);
        return CommandResult.noResult(assetId);
    }

    private String resolveAssetId(CreateAssetCommand command) {
        if (command.id() != null && !command.id().isBlank()) {
            return command.id();
        }
        return UUID.randomUUID().toString();
    }

    private String persistAsset(Asset asset) {
        Objects.requireNonNull(asset, "asset");
        log.info("Adding asset: type={}, id={}", asset.getType(), asset.getId());
        AssetEntity entity = assetMapper.toEntity(asset);
        if (entity.getAttributes() != null && !entity.getAttributes().isEmpty()) {
            assetRepo.save(entity);
        } else {
            insertAllOnCreate(entity, asset.getAttributesFlat());
        }
        return entity.getId();
    }

    private void applyPatch(String id, AssetPatch patch) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(patch, "patch");
        AssetEntity entity = assetRepo.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + id));

        if (patch.status() != null) entity.setStatus(patch.status());
        if (patch.subtype() != null) entity.setSubtype(patch.subtype());
        if (patch.notionalAmount() != null) entity.setNotionalAmount(patch.notionalAmount());
        if (patch.year() != null) entity.setYear(patch.year());
        if (patch.description() != null) entity.setDescription(patch.description());
        if (patch.currency() != null) entity.setCurrency(patch.currency());
        assetRepo.save(entity);

        if (patch.attributes() != null) {
            updateAsset(entity, patch.attributes());
        }
    }

    void deleteAsset(String id) {
        Objects.requireNonNull(id, "id");
        AssetEntity e = assetRepo.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + id));
        e.setDeleted(1);
        assetRepo.save(e);
    }

    private void updateAsset(AssetEntity asset, Collection<AttributeValue<?>> attributes) {
        Objects.requireNonNull(asset, "asset entity");
        Objects.requireNonNull(attributes, "attributes");
        if (attributes.isEmpty()) return;
        Map<String, AttributeEntity> existing = new HashMap<>();
        for (AttributeEntity a : asset.getAttributes()) {
            if (a != null) {
                existing.put(a.getName(), a);
            }
        }
        for (AttributeValue<?> av : attributes) {
            if (av == null) continue;
            AttributeEntity cur = existing.get(av.name());
            if (cur == null) {
                AttributeEntity created = attributeMapper.toEntity(asset, av);
                asset.getAttributes().add(created);
                attributeRepo.save(created);
                existing.put(created.getName(), created);
            } else {
                AttributeValue<?> existingAv = attributeMapper.toModel(cur);
                if (AttributeComparator.checkforUpdates(existingAv, av)) {
                    AttributeUpdater.apply(cur, av);
                    attributeRepo.save(cur);
                }
            }
        }
    }

    private void insertAllOnCreate(AssetEntity asset, Collection<AttributeValue<?>> attributes) {
        int incoming = (attributes != null) ? attributes.size() : -1;
        log.info("insertAllOnCreate(): incomingAttrCount={}", incoming);
        if (attributes != null) {
            for (AttributeValue<?> av : attributes) {
                if (av == null) continue;
                AttributeEntity e = attributeMapper.toEntity(asset, av);
                asset.getAttributes().add(e);
            }
        }
        assetRepo.save(asset);
    }

    private void recordCommand(String assetId, AssetCommand<?> command) {
        Objects.requireNonNull(command, "command");
        String commandType = command.commandType();
        String payload;
        try {
            payload = objectMapper.writeValueAsString(command);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialise {} command for asset {}", commandType, assetId, e);
            payload = String.valueOf(command);
        }

        CommandLogEntity entity = CommandLogEntity.builder()
                .commandType(commandType)
                .assetId(assetId)
                .payload(payload)
                .createdAt(Instant.now())
                .build();

        commandLogRepository.save(entity);
    }
}
