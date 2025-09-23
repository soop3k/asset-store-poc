package com.db.assetstore.domain.service;

import com.db.assetstore.domain.model.AssetId;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Application-layer command to patch an existing asset.
 * Null common fields mean: do not change. Attributes contain SET/CLEAR (value=null clears).
 */
@Builder
public record PatchAssetCommand(
        AssetId assetId,
        String status,
        String subtype,
        BigDecimal notionalAmount,
        Integer year,
        String description,
        String currency,
        List<AttributeValue<?>> attributes,
        String modifiedBy,
        Instant requestTime
) {}
