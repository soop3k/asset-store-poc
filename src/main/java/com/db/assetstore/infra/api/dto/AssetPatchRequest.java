package com.db.assetstore.infra.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.math.BigDecimal;

/**
 * HTTP-layer DTO for asset update requests.
 * Pure JSON DTO: contains Jackson annotations and no domain logic.
 * attributes is a flat object {name: value} matching existing API shape.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetPatchRequest {
    private String id;
    private String status;
    private String subtype;
    private BigDecimal notionalAmount;
    private Integer year;
    private String description;
    private String currency;

    private JsonNode attributes;

    private String executedBy;
}
