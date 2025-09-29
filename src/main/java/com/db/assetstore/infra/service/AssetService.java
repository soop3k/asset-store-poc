package com.db.assetstore.infra.service;

import com.db.assetstore.domain.model.asset.Asset;
import com.db.assetstore.domain.model.asset.AssetPatch;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.service.cmd.CommandResult;
import com.db.assetstore.domain.service.asset.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.asset.cmd.DeleteAssetCommand;
import com.db.assetstore.domain.service.asset.cmd.PatchAssetCommand;
import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.infra.jpa.AttributeEntity;
import com.db.assetstore.infra.mapper.AssetMapper;
import com.db.assetstore.infra.mapper.AttributeMapper;
import com.db.assetstore.infra.repository.AssetRepository;
import com.db.assetstore.infra.repository.AttributeRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetMapper assetMapper;
    private final AttributeMapper attributeMapper;
    private final AssetRepository assetRepo;
    private final AttributeRepository attributeRepo;

    public CommandResult<String> create(@NonNull CreateAssetCommand command) {
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

    public CommandResult<Void> patch(@NonNull PatchAssetCommand command) {
        String assetId = command.assetId();
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

    public CommandResult<Void> delete(@NonNull DeleteAssetCommand command) {
        String assetId = command.assetId();
        deleteAsset(assetId);
        return CommandResult.noResult(assetId);
    }

    private String resolveAssetId(CreateAssetCommand command) {
        if (command.id() != null && !command.id().isBlank()) {
            return command.id();
        }
        return UUID.randomUUID().toString();
    }

    private String persistAsset(@NonNull Asset asset) {
        log.info("Adding asset: type={}, id={}", asset.getType(), asset.getId());
        AssetEntity entity = assetMapper.toEntity(asset);
        var existingAttributes = entity.getAttributes();
        if (existingAttributes != null && !existingAttributes.isEmpty()) {
            assetRepo.save(entity);
        } else {
            persistAttributes(entity, asset.getAttributesFlat());
        }
        return entity.getId();
    }

    private void applyPatch(@NonNull String id, @NonNull AssetPatch patch, String executedBy) {
        AssetEntity entity = assetRepo.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + id));

        Optional.ofNullable(patch.status()).ifPresent(entity::setStatus);
        Optional.ofNullable(patch.subtype()).ifPresent(entity::setSubtype);
        Optional.ofNullable(patch.notionalAmount()).ifPresent(entity::setNotionalAmount);
        Optional.ofNullable(patch.year()).ifPresent(entity::setYear);
        Optional.ofNullable(patch.description()).ifPresent(entity::setDescription);
        Optional.ofNullable(patch.currency()).ifPresent(entity::setCurrency);

        entity.setModifiedBy(executedBy);
        entity.setModifiedAt(Instant.now());
        entity = assetRepo.save(entity);

        var patchAttributes = patch.attributes();
        if (patchAttributes != null && !patchAttributes.isEmpty()) {
            updateAsset(entity, patchAttributes);
        }
    }

    private void deleteAsset(@NonNull String id) {
        AssetEntity entity = assetRepo.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + id));
        entity.setDeleted(1);
        assetRepo.save(entity);
    }

    private void updateAsset(@NonNull AssetEntity asset, @NonNull Collection<AttributeValue<?>> attributes) {
        if (attributes.isEmpty()) {
            return;
        }

        Map<String, AttributeEntity> existing = new HashMap<>();
        var currentEntities = asset.getAttributes();
        if (currentEntities != null) {
            for (var attribute : currentEntities) {
                if (attribute != null) {
                    existing.put(attribute.getName(), attribute);
                }
            }
        }

        for (AttributeValue<?> incoming : attributes) {
            if (incoming == null) {
                continue;
            }
            AttributeEntity current = existing.get(incoming.name());
            if (current == null) {
                AttributeEntity created = attributeMapper.toEntity(asset, incoming);
                asset.getAttributes().add(created);
                attributeRepo.save(created);
                existing.put(created.getName(), created);
            } else {
                existing.put(current.getName(), current);

                AttributeValue<?> currentValue = attributeMapper.toModel(current);
                if (AttributeComparator.checkForUpdates(currentValue, incoming)) {
                    AttributeUpdater.apply(current, incoming);
                    attributeRepo.save(current);
                }
            }
        }
    }

    private void persistAttributes(AssetEntity asset, Collection<AttributeValue<?>> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            assetRepo.save(asset);
            return;
        }
        for (var attributeValue : attributes) {
            if (attributeValue == null) {
                continue;
            }
            AttributeEntity entity = attributeMapper.toEntity(asset, attributeValue);
            asset.getAttributes().add(entity);
        }
        assetRepo.save(asset);
    }
}
