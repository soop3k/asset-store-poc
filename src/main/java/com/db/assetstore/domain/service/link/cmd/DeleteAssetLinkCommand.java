package com.db.assetstore.domain.service.link.cmd;

import lombok.Builder;

import java.time.Instant;

/**
 * Command representing a soft delete of an existing link instance.
 */
@Builder
public record DeleteAssetLinkCommand(
        String assetId,
        String linkId,
        String requestedBy,
        Instant requestTime
) {
}
