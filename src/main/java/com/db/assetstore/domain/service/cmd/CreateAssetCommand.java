package com.db.assetstore.domain.service.cmd;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import lombok.Builder;
import lombok.Singular;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Application-layer command representing a request to create an Asset.
 * Attributes are already parsed into domain AttributeValue list.
 */
@Builder
public record CreateAssetCommand(
        String id,
        AssetType type,
        String status,
        String subtype,
        BigDecimal notionalAmount,
        Integer year,
        String description,
        String currency,
        @Singular("attribute") List<AttributeValue<?>> attributes,
        String createdBy,
        Instant requestTime
) {}
