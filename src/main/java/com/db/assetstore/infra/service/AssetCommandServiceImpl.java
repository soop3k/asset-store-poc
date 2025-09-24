package com.db.assetstore.infra.service;

import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.AssetPatch;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;
import com.db.assetstore.domain.service.link.cmd.CreateAssetLinkCommand;
import com.db.assetstore.domain.service.link.cmd.DeleteAssetLinkCommand;
import com.db.assetstore.domain.service.link.cmd.PatchAssetLinkCommand;
import com.db.assetstore.domain.service.AssetCommandService;
import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.infra.jpa.AttributeEntity;
import com.db.assetstore.infra.jpa.link.AssetLinkEntity;
import com.db.assetstore.infra.jpa.link.LinkDefinitionEntity;
import com.db.assetstore.infra.mapper.AssetMapper;
import com.db.assetstore.infra.mapper.AttributeMapper;
import com.db.assetstore.infra.repository.AssetRepository;
import com.db.assetstore.infra.repository.AttributeRepository;
import com.db.assetstore.infra.repository.link.AssetLinkRepository;
import com.db.assetstore.infra.repository.link.LinkDefinitionRepository;
import com.db.assetstore.infra.service.link.AssetLinkValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * Command-side implementation operating on Asset entities.
 * Implements creation, updates (common fields and attributes), and soft delete.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetCommandServiceImpl implements AssetCommandService {

    private final AssetMapper assetMapper;
    private final AttributeMapper attributeMapper;
    private final AssetRepository assetRepo;
    private final AttributeRepository attributeRepo;
    private final AssetLinkRepository assetLinkRepository;
    private final LinkDefinitionRepository linkDefinitionRepository;
    private final AssetLinkValidationService assetLinkValidationService;

    @Override
    @Transactional
    public String create(CreateAssetCommand command) {
        Objects.requireNonNull(command, "command");
        String id = (command.id() != null && !command.id().isBlank()) ? command.id() : java.util.UUID.randomUUID().toString();
        Asset asset = Asset.builder()
                .id(id)
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

        return create(asset);
    }

    @Transactional
    public String create(Asset asset) {
        Objects.requireNonNull(asset, "asset");
        log.info("Adding asset: type={}, id={}", asset.getType(), asset.getId());
        AssetEntity e = assetMapper.toEntity(asset);
        if (e.getAttributes() != null && !e.getAttributes().isEmpty()) {
            assetRepo.save(e);
        } else {
            insertAllOnCreate(e, asset.getAttributesFlat());
        }
        return e.getId();
    }

    @Override
    @Transactional
    public void update(PatchAssetCommand command) {
        Objects.requireNonNull(command, "command");
        AssetPatch patch = AssetPatch.builder()
                .status(command.status())
                .subtype(command.subtype())
                .notionalAmount(command.notionalAmount())
                .year(command.year())
                .description(command.description())
                .currency(command.currency())
                .attributes(command.attributes())
                .build();
        update(command.assetId(), patch);
    }

    @Transactional
    public void update(String id, AssetPatch patch) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(patch, "patch");
        AssetEntity e = assetRepo.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + id));

        // Apply common fields if present
        if (patch.status() != null) e.setStatus(patch.status());
        if (patch.subtype() != null) e.setSubtype(patch.subtype());
        if (patch.notionalAmount() != null) e.setNotionalAmount(patch.notionalAmount());
        if (patch.year() != null) e.setYear(patch.year());
        if (patch.description() != null) e.setDescription(patch.description());
        if (patch.currency() != null) e.setCurrency(patch.currency());
        assetRepo.save(e);

        if (patch.attributes() != null) {
            updateAsset(e, patch.attributes());
        }
    }

    @Transactional
    public void delete(String id) {
        Objects.requireNonNull(id, "id");
        AssetEntity e = assetRepo.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + id));
        e.setDeleted(1);
        assetRepo.save(e);
    }

    @Override
    @Transactional
    public String createLink(CreateAssetLinkCommand command) {
        Objects.requireNonNull(command, "command");
        AssetEntity asset = assetRepo.findByIdAndDeleted(command.assetId(), 0)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + command.assetId()));

        LinkDefinitionEntity definition = linkDefinitionRepository.findByCodeIgnoreCase(command.linkCode())
                .orElseThrow(() -> new IllegalArgumentException("Unknown link code: " + command.linkCode()));

        assetLinkValidationService.validate(definition, command);

        if (assetLinkRepository.existsByAssetIdAndEntityTypeAndEntityIdAndLinkCodeAndLinkSubtypeAndDeletedIsFalse(
                asset.getId(), command.entityType(), command.entityId(), definition.getCode(), command.linkSubtype())) {
            throw new IllegalStateException("Link already exists for asset %s and entity %s".formatted(asset.getId(), command.entityId()));
        }

        AssetLinkEntity entity = buildLinkEntity(asset.getId(), definition.getCode(), command);
        assetLinkRepository.save(entity);
        return entity.getId();
    }

    @Override
    @Transactional
    public void deleteLink(DeleteAssetLinkCommand command) {
        Objects.requireNonNull(command, "command");
        AssetLinkEntity entity = assetLinkRepository.findByIdAndDeletedIsFalse(command.linkId())
                .orElseThrow(() -> new IllegalArgumentException("Asset link not found: " + command.linkId()));
        if (!entity.getAssetId().equals(command.assetId())) {
            throw new IllegalArgumentException("Link %s does not belong to asset %s".formatted(command.linkId(), command.assetId()));
        }
        Instant now = Optional.ofNullable(command.requestTime()).orElseGet(Instant::now);
        entity.setActive(false);
        entity.setDeleted(true);
        entity.setValidTo(now);
        entity.setModifiedAt(now);
        entity.setModifiedBy(command.requestedBy());
        assetLinkRepository.save(entity);
    }

    @Override
    @Transactional
    public void patchLink(PatchAssetLinkCommand command) {
        Objects.requireNonNull(command, "command");

        AssetLinkEntity entity = assetLinkRepository.findByIdAndDeletedIsFalse(command.linkId())
                .orElseThrow(() -> new IllegalArgumentException("Asset link not found: " + command.linkId()));
        if (!entity.getAssetId().equals(command.assetId())) {
            throw new IllegalArgumentException("Link %s does not belong to asset %s".formatted(command.linkId(), command.assetId()));
        }

        Instant now = Optional.ofNullable(command.requestTime()).orElseGet(Instant::now);

        if (command.hasActiveChange() && command.active() != entity.isActive()) {
            if (Boolean.TRUE.equals(command.active())) {
                LinkDefinitionEntity definition = linkDefinitionRepository.findByCodeIgnoreCase(entity.getLinkCode())
                        .orElseThrow(() -> new IllegalStateException("Link definition not found for code: " + entity.getLinkCode()));
                assetLinkValidationService.validate(definition, entity.getAssetId(), entity.getEntityType(), entity.getEntityId(), entity.getLinkSubtype(), true);
                entity.setActive(true);
                entity.setDeleted(false);
            } else {
                entity.setActive(false);
            }
        }

        if (command.validFrom() != null) {
            entity.setValidFrom(command.validFrom());
        }
        if (command.validTo() != null) {
            entity.setValidTo(command.validTo());
        }

        entity.setModifiedAt(now);
        entity.setModifiedBy(command.requestedBy());
        assetLinkRepository.save(entity);
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

    private AssetLinkEntity buildLinkEntity(String assetId, String linkCode, CreateAssetLinkCommand command) {
        Instant now = Optional.ofNullable(command.requestTime()).orElseGet(Instant::now);
        Instant validFrom = Optional.ofNullable(command.validFrom()).orElse(now);
        boolean active = command.active() == null || command.active();
        return AssetLinkEntity.builder()
                .id(UUID.randomUUID().toString())
                .assetId(assetId)
                .linkCode(linkCode)
                .linkSubtype(command.linkSubtype())
                .entityType(command.entityType())
                .entityId(command.entityId())
                .active(active)
                .deleted(false)
                .validFrom(validFrom)
                .validTo(command.validTo())
                .createdAt(now)
                .createdBy(command.requestedBy())
                .modifiedAt(now)
                .modifiedBy(command.requestedBy())
                .build();
    }
}
