package com.db.assetstore.domain.service.link.cmd.factory;

import com.db.assetstore.domain.service.link.cmd.CreateAssetLinkCommand;
import com.db.assetstore.domain.service.link.cmd.DeleteAssetLinkCommand;
import com.db.assetstore.domain.service.link.cmd.PatchAssetLinkCommand;
import com.db.assetstore.infra.api.dto.AssetLinkCreateRequest;
import com.db.assetstore.infra.api.dto.AssetLinkPatchRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;

/**
 * Factory translating transport DTOs into command objects for asset links.
 */
@Component
public class AssetLinkCommandFactory {

    public CreateAssetLinkCommand createCommand(String assetId, AssetLinkCreateRequest request) {
        Objects.requireNonNull(assetId, "assetId");
        Objects.requireNonNull(request, "request");
        return CreateAssetLinkCommand.builder()
                .assetId(assetId)
                .linkCode(request.getLinkCode())
                .linkSubtype(request.getLinkSubtype())
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .active(request.getActive())
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .requestedBy(request.getRequestedBy())
                .requestTime(Instant.now())
                .build();
    }

    public DeleteAssetLinkCommand deleteCommand(String assetId, String linkId, String requestedBy) {
        Objects.requireNonNull(assetId, "assetId");
        Objects.requireNonNull(linkId, "linkId");
        return DeleteAssetLinkCommand.builder()
                .assetId(assetId)
                .linkId(linkId)
                .requestedBy(requestedBy)
                .requestTime(Instant.now())
                .build();
    }

    public PatchAssetLinkCommand patchCommand(String assetId, String linkId, AssetLinkPatchRequest request) {
        Objects.requireNonNull(assetId, "assetId");
        Objects.requireNonNull(linkId, "linkId");
        Objects.requireNonNull(request, "request");
        return PatchAssetLinkCommand.builder()
                .assetId(assetId)
                .linkId(linkId)
                .active(request.getActive())
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .requestedBy(request.getRequestedBy())
                .requestTime(Instant.now())
                .build();
    }
}
