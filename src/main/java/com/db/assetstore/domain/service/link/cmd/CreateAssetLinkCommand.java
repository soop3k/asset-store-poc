package com.db.assetstore.domain.service.link.cmd;

import lombok.Builder;

import java.time.Instant;

/**
 * Command describing creation of a link between an asset and an external entity.
 */
@Builder
public record CreateAssetLinkCommand(
        String assetId,
        String linkCode,
        String linkSubtype,
        String entitySubtype,
        String entityType,
        String entityId,
        Boolean active,
        Instant validFrom,
        Instant validTo,
        String requestedBy,
        Instant requestTime
) {
}
