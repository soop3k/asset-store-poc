package com.db.assetstore.domain.model.link;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetLink {
    private final Long id;
    private final String assetId;
    private final String entityType;
    private final String entitySubtype;
    private final String targetCode;
    private final boolean active;
    private final Instant createdAt;
    private final String createdBy;
    private final Instant deactivatedAt;
    private final String deactivatedBy;
}
