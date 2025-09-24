package com.db.assetstore.infra.api.dto;

import com.db.assetstore.AssetType;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;

public record AssetCreateRequest(
        String id,
        @NonNull
        AssetType type,
        String status,
        String subtype,
        BigDecimal notionalAmount,
        Integer year,
        String description,
        String currency,
        JsonNode attributes,
        String executedBy
) {}
