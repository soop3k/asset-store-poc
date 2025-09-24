package com.db.assetstore.domain.service.link.cmd;

import lombok.Builder;

import java.time.Instant;

/**
 * Command describing partial updates to an existing asset link.
 */
@Builder
public record PatchAssetLinkCommand(
        String assetId,
        String linkId,
        Boolean active,
        Instant validFrom,
        Instant validTo,
        String requestedBy,
        Instant requestTime
) {
    public boolean hasActiveChange() {
        return active != null;
    }
}
