package com.db.assetstore.domain.model.link;

import lombok.Builder;
import lombok.Singular;

import java.util.Set;

/**
 * Domain representation of link definition metadata.
 */
@Builder(toBuilder = true)
public record LinkDefinition(
        String code,
        String entityType,
        LinkCardinality cardinality,
        boolean enabled,
        @Singular Set<String> entityTypes
) {
}
