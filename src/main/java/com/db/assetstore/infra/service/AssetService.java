package com.db.assetstore.infra.service;

import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.AssetPatch;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.service.cmd.CommandResult;
import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.cmd.DeleteAssetCommand;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;
import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.infra.jpa.AttributeEntity;
import com.db.assetstore.infra.mapper.AssetMapper;
import com.db.assetstore.infra.mapper.AttributeMapper;
import com.db.assetstore.infra.repository.AssetRepository;
import com.db.assetstore.infra.repository.AttributeRepository;
import com.db.assetstore.infra.service.AttributeComparator;
import com.db.assetstore.infra.service.AttributeUpdater;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetMapper assetMapper;
    private final AttributeMapper attributeMapper;
    private final AssetRepository assetRepo;
    private final AttributeRepository attributeRepo;

    public CommandResult<String> create(CreateAssetCommand command) {
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

    public CommandResult<Void> patch(PatchAssetCommand command) {
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

    public CommandResult<Void> delete(DeleteAssetCommand command) {
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
        entity = assetRepo.save(entity);

        if (patch.attributes() != null) {
            updateAsset(entity, patch.attributes());
        }
    }

    private void deleteAsset(String id) {
        Objects.requireNonNull(id, "id");
        AssetEntity entity = assetRepo.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + id));
        entity.setDeleted(1);
        assetRepo.save(entity);
    }

    private void updateAsset(AssetEntity asset, Collection<AttributeValue<?>> attributes) {
        Objects.requireNonNull(asset, "asset entity");
        Objects.requireNonNull(attributes, "attributes");
        if (attributes.isEmpty()) {
            return;
        }

        Map<String, AttributeEntity> existing = new HashMap<>();
        for (AttributeEntity attribute : asset.getAttributes()) {
            if (attribute != null) {
                existing.put(attribute.getName(), attribute);
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
                if (AttributeComparator.checkforUpdates(currentValue, incoming)) {
                    AttributeUpdater.apply(current, incoming);
                    attributeRepo.save(current);
                }
            }
        }
    }

    private void insertAllOnCreate(AssetEntity asset, Collection<AttributeValue<?>> attributes) {
        int incoming = attributes != null ? attributes.size() : -1;
        log.info("insertAllOnCreate(): incomingAttrCount={}", incoming);
        if (attributes != null) {
            for (AttributeValue<?> attributeValue : attributes) {
                if (attributeValue == null) {
                    continue;
                }
                AttributeEntity entity = attributeMapper.toEntity(asset, attributeValue);
                asset.getAttributes().add(entity);
            }
        }
        assetRepo.save(asset);
    }
}
