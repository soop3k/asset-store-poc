package com.db.assetstore.infra.service.link;

import com.db.assetstore.domain.service.cmd.CommandResult;
import com.db.assetstore.domain.service.link.cmd.CreateAssetLinkCommand;
import com.db.assetstore.domain.service.link.cmd.DeleteAssetLinkCommand;
import com.db.assetstore.infra.jpa.AssetLinkEntity;
import com.db.assetstore.infra.jpa.LinkDefinitionEntity;
import com.db.assetstore.infra.repository.AssetLinkRepo;
import com.db.assetstore.infra.repository.LinkDefinitionRepo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetLinkService {

    private final AssetLinkRepo assetLinkRepo;
    private final LinkDefinitionRepo linkDefinitionRepo;
    private final AssetLinkCommandValidator assetLinkCommandValidator;

    public CommandResult<Long> create(@NonNull CreateAssetLinkCommand command) {
        LinkDefinitionEntity definition = findActiveDefinition(command);
        assetLinkCommandValidator.validateCreate(command, definition);

        LinkPersistenceResult result = persistLink(command);
        log.info("{} link id={} for asset {}",
                result.reactivated() ? "Reactivated" : "Created",
                result.entity().getId(), command.assetId());

        return new CommandResult<>(result.entity().getId(), command.assetId());
    }

    public CommandResult<Void> delete(@NonNull DeleteAssetLinkCommand command) {
        AssetLinkEntity entity = requireActiveLink(command);
        deactivateLink(command, entity);
        log.info("Deactivated link id={} for asset {}", entity.getId(), command.assetId());
        return CommandResult.noResult(command.assetId());
    }

    private LinkDefinitionEntity findActiveDefinition(CreateAssetLinkCommand command) {
        return linkDefinitionRepo
                .findByEntityTypeAndEntitySubtypeAndActiveTrue(command.entityType(), command.entitySubtype())
                .orElseThrow(() -> new IllegalStateException(
                        "Link definition missing for %s/%s".formatted(command.entityType(), command.entitySubtype())));
    }

    private LinkPersistenceResult persistLink(CreateAssetLinkCommand command) {
        Optional<AssetLinkEntity> existing = assetLinkRepo.link(
                command.assetId(), command.entityType(), command.entitySubtype(), command.targetCode());
        boolean reactivated = existing.filter(link -> !link.isActive()).isPresent();

        AssetLinkEntity entity = existing
                .map(existingLink -> reactivate(existingLink, command))
                .orElseGet(() -> newAssetLink(command));

        AssetLinkEntity saved = assetLinkRepo.save(entity);
        return new LinkPersistenceResult(saved, reactivated);
    }

    private AssetLinkEntity newAssetLink(CreateAssetLinkCommand command) {
        return AssetLinkEntity.builder()
                .assetId(command.assetId())
                .entityType(command.entityType())
                .entitySubtype(command.entitySubtype())
                .targetCode(command.targetCode())
                .active(true)
                .createdAt(orNow(command.requestTime()))
                .createdBy(command.executedBy())
                .build();
    }

    private AssetLinkEntity requireActiveLink(DeleteAssetLinkCommand command) {
        return assetLinkRepo
                .activeLink(
                        command.assetId(), command.entityType(), command.entitySubtype(), command.targetCode())
                .orElseThrow(() -> new IllegalStateException(
                        "Active link not found for asset %s".formatted(command.assetId())));
    }

    private void deactivateLink(DeleteAssetLinkCommand command, AssetLinkEntity entity) {
        entity.setActive(false);
        entity.setDeactivatedAt(orNow(command.requestTime()));
        entity.setDeactivatedBy(command.executedBy());
        assetLinkRepo.save(entity);
    }

    private AssetLinkEntity reactivate(AssetLinkEntity entity, CreateAssetLinkCommand command) {
        if (entity.isActive()) {
            throw new IllegalStateException(
                    "Active link already exists for asset %s and target %s".formatted(
                            command.assetId(), command.targetCode()));
        }
        entity.setActive(true);
        entity.setCreatedAt(orNow(command.requestTime()));
        entity.setCreatedBy(command.executedBy());
        entity.setDeactivatedAt(null);
        entity.setDeactivatedBy(null);
        return entity;
    }

    private Instant orNow(Instant instant) {
        return instant != null ? instant : Instant.now();
    }

    private record LinkPersistenceResult(AssetLinkEntity entity, boolean reactivated) {
    }
}
