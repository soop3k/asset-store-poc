package com.db.assetstore.domain.model.asset;

import java.math.BigDecimal;
import java.time.Instant;

public record AssetHistory(
        String assetId,
        String status,
        String subtype,
        Instant statusEffectiveTime,
        BigDecimal notionalAmount,
        Integer year,
        String description,
        String currency,
        String modifiedBy,
        Instant modifiedAt,
        String createdBy,
        Instant createdAt,
        String wh,
        String sourceSystemName,
        String externalReference,
        boolean deleted,
        Instant changedAt
) {
}
