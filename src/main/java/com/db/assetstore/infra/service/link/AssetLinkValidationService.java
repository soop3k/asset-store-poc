package com.db.assetstore.infra.service.link;

import com.db.assetstore.domain.service.link.cmd.CreateAssetLinkCommand;
import com.db.assetstore.infra.jpa.link.LinkDefinitionEntity;
import com.db.assetstore.infra.jpa.link.LinkSubtypeDefinitionEntity;
import com.db.assetstore.infra.repository.link.AssetLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssetLinkValidationService {

    private final AssetLinkRepository assetLinkRepository;

    public void validateDefinition(LinkDefinitionEntity definition, CreateAssetLinkCommand command) {
        validateDefinition(definition, command.entityType(), command.entityId(), command.linkSubtype());
    }

    public void validateDefinition(LinkDefinitionEntity definition, String entityType, String entityId, String linkSubtype) {
        if (!definition.isEnabled()) {
            throw new IllegalStateException("Link definition %s is disabled".formatted(definition.getCode()));
        }
        if (entityType == null || entityType.isBlank()) {
            throw new IllegalArgumentException("Entity type must be provided");
        }
        if (entityId == null || entityId.isBlank()) {
            throw new IllegalArgumentException("Entity id must be provided");
        }
        if (!definition.getEntityType().equalsIgnoreCase(entityType)) {
            throw new IllegalArgumentException("Entity type %s not allowed for link %s".formatted(entityType, definition.getCode()));
        }
        if (linkSubtype == null || linkSubtype.isBlank()) {
            throw new IllegalArgumentException("Link subtype must be provided");
        }
        var subtypes = definition.getSubtypes();
        if (subtypes == null || subtypes.isEmpty()) {
            throw new IllegalStateException("No subtypes configured for link %s".formatted(definition.getCode()));
        }
        boolean subtypeAllowed = subtypes.stream()
                .map(LinkSubtypeDefinitionEntity::getId)
                .map(id -> id.getSubtype().toUpperCase())
                .anyMatch(code -> code.equalsIgnoreCase(linkSubtype));
        if (!subtypeAllowed) {
            throw new IllegalArgumentException("Subtype %s not allowed for link %s".formatted(linkSubtype, definition.getCode()));
        }
    }

    public void validateCardinality(LinkDefinitionEntity definition, CreateAssetLinkCommand command) {
        validateCardinality(definition, command.assetId(), command.entityType(), command.entityId(), command.linkSubtype());
    }

    public void validateCardinality(LinkDefinitionEntity definition, String assetId, String entityType, String entityId, String linkSubtype) {
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

