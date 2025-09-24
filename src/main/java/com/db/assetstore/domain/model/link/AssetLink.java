package com.db.assetstore.domain.model.link;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Domain view of an asset link.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AssetLink(
    String id,
    String assetId,
    String linkCode,
    String linkSubtype,
    String entityType,
    String entityId,
    boolean active,
    boolean deleted,
    Instant validFrom,
    Instant validTo,
    Instant createdAt,
    String createdBy,
    Instant modifiedAt,
    String modifiedBy
) {
}
