package com.db.assetstore.infra.service.link;

import com.db.assetstore.domain.exception.DomainValidationException;
import com.db.assetstore.domain.exception.LinkConflictException;
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
        validate(definition, command.assetId(), command.entityType(), command.entityId(), command.entitySubtype(), command.linkSubtype(), activate);
    }

    public void validate(LinkDefinitionEntity definition,
                         String assetId,
                         String entityType,
                         String entityId,
                         String entitySubtype,
                         String linkSubtype,
                         boolean activate) {
        validateDefinition(definition, entityType, entityId, entitySubtype, linkSubtype);
        if (activate) {
            validateCardinality(definition, assetId, entityType, entityId, entitySubtype, linkSubtype);
        }
    }

    private void validateDefinition(LinkDefinitionEntity definition,
                                    String entityType,
                                    String entityId,
                                    String entitySubtype,
                                    String linkSubtype) {
        if (!definition.isEnabled()) {
            throw new DomainValidationException("Link definition %s is disabled".formatted(definition.getCode()));
        }
        if (entityType == null || entityType.isBlank()) {
            throw new DomainValidationException("Entity type must be provided");
        }
        if (entityId == null || entityId.isBlank()) {
            throw new DomainValidationException("Entity id must be provided");
        }
        if (entitySubtype == null || entitySubtype.isBlank()) {
            throw new DomainValidationException("Entity subtype must be provided");
        }
        if (linkSubtype == null || linkSubtype.isBlank()) {
            throw new DomainValidationException("Link subtype must be provided");
        }
        var configuredTypes = definition.getAllowedEntityTypes();
        if (configuredTypes == null || configuredTypes.isEmpty()) {
            throw new DomainValidationException("No entity types configured for link %s".formatted(definition.getCode()));
        }
        boolean entityAllowed = configuredTypes.stream()
                .filter(type -> type != null && !type.isBlank())
                .anyMatch(allowed -> allowed.equalsIgnoreCase(entityType));
        if (!entityAllowed) {
            throw new DomainValidationException("Entity type %s not allowed for link %s".formatted(entityType, definition.getCode()));
        }
    }

    private void validateCardinality(LinkDefinitionEntity definition,
                                     String assetId,
                                     String entityType,
                                     String entityId,
                                     String entitySubtype,
                                     String linkSubtype) {
        long assetActive = assetLinkRepository.countByAssetIdAndLinkCodeAndLinkSubtypeAndEntitySubtypeAndActiveIsTrueAndDeletedIsFalse(
                assetId, definition.getCode(), linkSubtype, entitySubtype);
        long entityActive = assetLinkRepository.countByEntityTypeAndEntityIdAndEntitySubtypeAndLinkCodeAndLinkSubtypeAndActiveIsTrueAndDeletedIsFalse(
                entityType, entityId, entitySubtype, definition.getCode(), linkSubtype);
        switch (definition.getCardinality()) {
            case ONE_TO_ONE -> {
                if (assetActive > 0 || entityActive > 0) {
                    throw new LinkConflictException("ONE_TO_ONE link already exists for asset %s or entity %s".formatted(assetId, entityId));
                }
            }
            case ONE_TO_MANY -> {
                if (entityActive > 0) {
                    throw new LinkConflictException("Entity %s already linked for ONE_TO_MANY".formatted(entityId));
                }
            }
            case MANY_TO_ONE -> {
                if (assetActive > 0) {
                    throw new LinkConflictException("Asset %s already linked for MANY_TO_ONE".formatted(assetId));
                }
            }
            default -> throw new DomainValidationException("Unsupported cardinality: " + definition.getCardinality());
        }
    }
}
