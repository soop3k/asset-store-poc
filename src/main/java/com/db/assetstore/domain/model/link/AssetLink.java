package com.db.assetstore.domain.model.link;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Domain view of an asset link.
 */
@Getter
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetLink {
    private final String id;
    private final String assetId;
    private final String linkCode;
    private final String linkSubtype;
    private final String entityType;
    private final String entityId;
    private final boolean active;
    private final boolean deleted;
    private final Instant validFrom;
    private final Instant validTo;
    private final Instant createdAt;
    private final String createdBy;
    private final Instant modifiedAt;
    private final String modifiedBy;
}
