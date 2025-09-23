package com.db.assetstore.domain.service.cmd;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Builder
public record PatchAssetCommand(
        String assetId,
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
