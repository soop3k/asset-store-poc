package com.db.assetstore.domain.model;

import com.db.assetstore.domain.model.attribute.AttributeValue;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Domain use-case DTO describing changes to apply to an Asset.
 * All fields are optional (null means: do not change this field).
 * Attributes list: each item represents a SET/CLEAR operation; value=null means CLEAR.
 * This class is in the domain/application layer and has no Jackson/JPA annotations.
 */
@Builder
public record AssetPatch(
        String status,
        String subtype,
        BigDecimal notionalAmount,
        Integer year,
        String description,
        String currency,
        List<AttributeValue<?>> attributes
) {}