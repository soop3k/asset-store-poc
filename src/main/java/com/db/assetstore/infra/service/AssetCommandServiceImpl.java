package com.db.assetstore.infra.service;

import com.db.assetstore.domain.service.CreateAssetCommand;
import com.db.assetstore.domain.service.PatchAssetCommand;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.AssetId;
import com.db.assetstore.domain.model.AssetPatch;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.domain.service.AssetCommandService;
import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.infra.jpa.AttributeEntity;
import com.db.assetstore.infra.mapper.AssetMapper;
import com.db.assetstore.infra.mapper.AttributeMapper;
import com.db.assetstore.infra.repository.AssetRepository;
import com.db.assetstore.infra.repository.AttributeRepository;
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

    @Override
    @Transactional
    public AssetId create(CreateAssetCommand command) {
        Objects.requireNonNull(command, "command");
        String id = (command.id() != null && !command.id().isBlank()) ? command.id() : java.util.UUID.randomUUID().toString();
        Asset asset = new Asset(
                id,
                command.type(),
                Instant.now(),
                AttributesCollection.fromFlat(command.attributes() == null ? List.of() : command.attributes())
        );
        asset.setStatus(command.status());
        asset.setSubtype(command.subtype());
        asset.setNotionalAmount(command.notionalAmount());
        asset.setYear(command.year());
        asset.setDescription(command.description());
        asset.setCurrency(command.currency());
        asset.setCreatedBy(command.createdBy());
        return create(asset);
    }

    @Override
    @Transactional
    public AssetId create(Asset asset) {
        Objects.requireNonNull(asset, "asset");
        log.info("Adding asset: type={}, id={}", asset.getType(), asset.getId());
        AssetEntity e = assetMapper.toEntity(asset);
        if (e.getAttributes() != null && !e.getAttributes().isEmpty()) {
            assetRepo.save(e);
        } else {
            insertAllOnCreate(e, asset.getAttributesFlat());
        }
        return AssetId.of(e.getId());
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

    @Override
    @Transactional
    public void updateAttributes(AssetId id, Collection<AttributeValue<?>> attrs) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(attrs, "attrs");
        AssetEntity e = assetRepo.findByIdAndDeleted(id.id(), 0)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + id.id()));
        updateAsset(e, attrs);
    }

    @Override
    @Transactional
    public void update(AssetId id, AssetPatch patch) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(patch, "patch");
        AssetEntity e = assetRepo.findByIdAndDeleted(id.id(), 0)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + id.id()));

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

    @Override
    @Transactional
    public void delete(AssetId id) {
        Objects.requireNonNull(id, "id");
        AssetEntity e = assetRepo.findByIdAndDeleted(id.id(), 0)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + id.id()));
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
}
