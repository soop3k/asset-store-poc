package com.db.assetstore.infra.service.link;

import com.db.assetstore.domain.exception.link.InactiveLinkDefinitionException;
import com.db.assetstore.domain.exception.link.LinkAlreadyExistsException;
import com.db.assetstore.domain.exception.link.LinkCardinalityViolationException;
import com.db.assetstore.domain.exception.link.LinkException;
import com.db.assetstore.domain.model.link.LinkCardinality;
import com.db.assetstore.domain.service.link.cmd.CreateAssetLinkCommand;
import com.db.assetstore.infra.jpa.AssetLinkEntity;
import com.db.assetstore.infra.jpa.LinkDefinitionEntity;
import com.db.assetstore.infra.repository.AssetLinkRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AssetLinkCommandValidator {

    private final AssetLinkRepo assetLinkRepo;

    public void validateCreate(CreateAssetLinkCommand command, LinkDefinitionEntity definition)
            throws LinkException {
        ensureDefinitionActive(definition);
        ensureNoDuplicate(command);
        ensureCardinality(definition.getCardinality(), command);
    }

    private void ensureDefinitionActive(LinkDefinitionEntity definition) throws InactiveLinkDefinitionException {
        if (!definition.isActive()) {
            throw new InactiveLinkDefinitionException(definition.getEntityType(), definition.getEntitySubtype());
        }
    }

    private void ensureNoDuplicate(CreateAssetLinkCommand command) throws LinkAlreadyExistsException {
        boolean exists = assetLinkRepo.activeLink(
                        command.assetId(), command.entityType(), command.entitySubtype(), command.targetCode())
                .isPresent();
        if (exists) {
            throw new LinkAlreadyExistsException(command.assetId(), command.targetCode());
        }
    }

    private void ensureCardinality(LinkCardinality cardinality, CreateAssetLinkCommand command)
            throws LinkCardinalityViolationException {
        List<AssetLinkEntity> assetLinks = assetLinkRepo
                .activeForAssetType(command.assetId(), command.entityType(), command.entitySubtype());
        List<AssetLinkEntity> targetLinks = assetLinkRepo
                .activeForTarget(command.entityType(), command.entitySubtype(), command.targetCode());

        if (cardinality.limitsAssetSide() && !assetLinks.isEmpty()) {
            throw new LinkCardinalityViolationException(
                    "Asset %s already has an active link for %s/%s".formatted(
                            command.assetId(), command.entityType(), command.entitySubtype()));
        }
        if (cardinality.limitsTargetSide() && !targetLinks.isEmpty()) {
            throw new LinkCardinalityViolationException(
                    "Target %s already linked for %s/%s".formatted(
                            command.targetCode(), command.entityType(), command.entitySubtype()));
        }
    }
}

