package com.db.assetstore.infra.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * HTTP payload used to create a new asset link instance.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetLinkCreateRequest {
    private String linkCode;
    private String linkSubtype;
    private String entityType;
    private String entityId;
    private Boolean active;
    private Instant validFrom;
    private Instant validTo;
    private String requestedBy;
}
