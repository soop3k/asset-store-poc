package com.db.assetstore.infra.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * HTTP payload used to patch an existing asset link instance.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetLinkPatchRequest {
    private Boolean active;
    private Instant validFrom;
    private Instant validTo;
    private String requestedBy;
}
