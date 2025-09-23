package com.db.assetstore.infra.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO for bulk patch items: includes the asset id and the same optional fields as AssetPatchRequest.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetPatchItemRequest {
    private String id;
    private String status;
    private String subtype;
    private BigDecimal notionalAmount;
    private Integer year;
    private String description;
    private String currency;
    private JsonNode attributes;
}
