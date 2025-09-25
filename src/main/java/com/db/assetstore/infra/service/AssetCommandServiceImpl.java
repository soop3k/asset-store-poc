package com.db.assetstore.infra.service;

import com.db.assetstore.domain.service.cmd.AssetCommand;
import com.db.assetstore.domain.service.cmd.AssetCommandVisitor;
import com.db.assetstore.domain.service.cmd.CommandResult;
import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.cmd.DeleteAssetCommand;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.AssetPatch;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.service.AssetCommandService;
import com.db.assetstore.domain.service.link.cmd.CreateAssetLinkCommand;
import com.db.assetstore.domain.service.link.cmd.DeleteAssetLinkCommand;
import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.infra.jpa.AssetLinkEntity;
import com.db.assetstore.infra.jpa.AttributeEntity;
import com.db.assetstore.infra.jpa.CommandLogEntity;
import com.db.assetstore.infra.jpa.LinkDefinitionEntity;
import com.db.assetstore.infra.mapper.AssetMapper;
import com.db.assetstore.infra.mapper.AttributeMapper;
import com.db.assetstore.infra.repository.AssetLinkRepository;
import com.db.assetstore.infra.repository.AssetRepository;
import com.db.assetstore.infra.repository.AttributeRepository;
import com.db.assetstore.infra.repository.CommandLogRepository;
import com.db.assetstore.infra.repository.LinkDefinitionRepository;
import com.db.assetstore.infra.service.link.AssetLinkCommandValidator;
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
    private final AssetLinkRepository assetLinkRepository;
    private final LinkDefinitionRepository linkDefinitionRepository;
    private final AssetLinkCommandValidator assetLinkCommandValidator;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public <R> CommandResult<R> execute(AssetCommand<R> command) {
        Objects.requireNonNull(command, "command");
        CommandResult<R> result = command.accept(this);
        if (result.success()) {
            recordCommand(result, command);
        }
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
                .createdBy(command.executedBy())
                .modifiedBy(command.executedBy())
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
        applyPatch(assetId, patch, command.executedBy());
        return CommandResult.noResult(assetId);
    }

    @Override
    public CommandResult<Void> visit(DeleteAssetCommand command) {
        Objects.requireNonNull(command, "command");
        String assetId = Objects.requireNonNull(command.assetId(), "assetId");
        deleteAsset(assetId);
        return CommandResult.noResult(assetId);
    }

    @Override
    public CommandResult<Long> visit(CreateAssetLinkCommand command) {
        Objects.requireNonNull(command, "command");

        LinkDefinitionEntity definition = linkDefinitionRepository
                .findByEntityTypeAndEntitySubtypeAndActiveTrue(command.entityType(), command.entitySubtype())
                .orElseThrow(() -> new IllegalStateException(
                        "Link definition missing for %s/%s".formatted(command.entityType(), command.entitySubtype())));

        assetLinkCommandValidator.validateCreate(command, definition);

        AssetLinkEntity entity = AssetLinkEntity.builder()
                .assetId(command.assetId())
                .entityType(command.entityType())
                .entitySubtype(command.entitySubtype())
                .targetCode(command.targetCode())
                .active(true)
                .createdAt(orNow(command.requestTime()))
                .createdBy(command.executedBy())
                .build();

        AssetLinkEntity saved = assetLinkRepository.save(entity);
        log.info("Created link id={} for asset {}", saved.getId(), command.assetId());
        return new CommandResult<>(saved.getId(), command.assetId());
    }

    @Override
    public CommandResult<Void> visit(DeleteAssetLinkCommand command) {
        Objects.requireNonNull(command, "command");

        AssetLinkEntity entity = assetLinkRepository
                .findByAssetIdAndEntityTypeAndEntitySubtypeAndTargetCodeAndActiveTrue(
                        command.assetId(), command.entityType(), command.entitySubtype(), command.targetCode())
                .orElseThrow(() -> new IllegalStateException(
                        "Active link not found for asset %s".formatted(command.assetId())));

        entity.setActive(false);
        entity.setDeactivatedAt(orNow(command.requestTime()));
        entity.setDeactivatedBy(command.executedBy());
        assetLinkRepository.save(entity);
        log.info("Deactivated link id={} for asset {}", entity.getId(), command.assetId());
        return CommandResult.noResult(command.assetId());
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

    private void applyPatch(String id, AssetPatch patch, String executedBy) {
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
        entity.setModifiedBy(executedBy);
        entity.setModifiedAt(Instant.now());
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

    private void recordCommand(CommandResult<?> result, AssetCommand<?> command) {
        Objects.requireNonNull(result, "result");
        Objects.requireNonNull(command, "command");
        String commandType = command.commandType();
        String payload;
        try {
            payload = objectMapper.writeValueAsString(command);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialise {} command for asset {}", commandType, result.assetId(), e);
            payload = String.valueOf(command);
        }

        CommandLogEntity entity = CommandLogEntity.builder()
                .commandType(commandType)
                .assetId(result.assetId())
                .payload(payload)
                .createdAt(Instant.now())
                .build();

        commandLogRepository.save(entity);
    }

    private Instant orNow(Instant instant) {
        return instant != null ? instant : Instant.now();
    }

}
