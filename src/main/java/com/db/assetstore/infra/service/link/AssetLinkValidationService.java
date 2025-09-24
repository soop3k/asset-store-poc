package com.db.assetstore.infra.service.link;

import com.db.assetstore.domain.service.link.cmd.CreateAssetLinkCommand;
import com.db.assetstore.infra.jpa.link.LinkDefinitionEntity;
import com.db.assetstore.infra.repository.link.AssetLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssetLinkValidationService {

    private final AssetLinkRepository assetLinkRepository;

    public void validate(LinkDefinitionEntity definition, CreateAssetLinkCommand command) {
        boolean activate = command.active() == null || command.active();
        validate(definition, command.assetId(), command.entityType(), command.entityId(), command.linkSubtype(), activate);
    }

    public void validate(LinkDefinitionEntity definition,
                         String assetId,
                         String entityType,
                         String entityId,
                         String linkSubtype,
                         boolean activate) {
        validateDefinition(definition, entityType, entityId, linkSubtype);
        if (activate) {
            validateCardinality(definition, assetId, entityType, entityId, linkSubtype);
        }
    }

    private void validateDefinition(LinkDefinitionEntity definition, String entityType, String entityId, String linkSubtype) {
        if (!definition.isEnabled()) {
            throw new IllegalStateException("Link definition %s is disabled".formatted(definition.getCode()));
        }
        if (entityType == null || entityType.isBlank()) {
            throw new IllegalArgumentException("Entity type must be provided");
        }
        if (entityId == null || entityId.isBlank()) {
            throw new IllegalArgumentException("Entity id must be provided");
        }
        if (linkSubtype == null || linkSubtype.isBlank()) {
            throw new IllegalArgumentException("Link subtype must be provided");
        }
        var configuredTypes = definition.getAllowedEntityTypes();
        if (configuredTypes == null || configuredTypes.isEmpty()) {
            throw new IllegalStateException("No entity types configured for link %s".formatted(definition.getCode()));
        }
        boolean entityAllowed = configuredTypes.stream()
                .map(type -> type.getId().getEntityType().toUpperCase())
                .anyMatch(allowed -> allowed.equalsIgnoreCase(entityType));
        if (!entityAllowed) {
            throw new IllegalArgumentException("Entity type %s not allowed for link %s".formatted(entityType, definition.getCode()));
        }
    }

    private void validateCardinality(LinkDefinitionEntity definition, String assetId, String entityType, String entityId, String linkSubtype) {
        long assetActive = assetLinkRepository.countByAssetIdAndLinkCodeAndLinkSubtypeAndActiveIsTrueAndDeletedIsFalse(
                assetId, definition.getCode(), linkSubtype);
        long entityActive = assetLinkRepository.countByEntityTypeAndEntityIdAndLinkCodeAndLinkSubtypeAndActiveIsTrueAndDeletedIsFalse(
                entityType, entityId, definition.getCode(), linkSubtype);
        switch (definition.getCardinality()) {
            case ONE_TO_ONE -> {
                if (assetActive > 0 || entityActive > 0) {
                    throw new IllegalStateException("ONE_TO_ONE link already exists for asset %s or entity %s".formatted(assetId, entityId));
                }
            }
            case ONE_TO_MANY -> {
                if (entityActive > 0) {
                    throw new IllegalStateException("Entity %s already linked for ONE_TO_MANY".formatted(entityId));
                }
            }
            case MANY_TO_ONE -> {
                if (assetActive > 0) {
                    throw new IllegalStateException("Asset %s already linked for MANY_TO_ONE".formatted(assetId));
                }
            }
            default -> throw new IllegalStateException("Unsupported cardinality: " + definition.getCardinality());
        }
    }
}
