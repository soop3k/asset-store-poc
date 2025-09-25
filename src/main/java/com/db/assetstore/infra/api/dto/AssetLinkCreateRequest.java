package com.db.assetstore.infra.api.dto;

/**
 * HTTP payload for creating a new Asset link to an external entity.
 */
public record AssetLinkCreateRequest(
        String entityType,
        String entitySubtype,
        String targetCode,
        String executedBy
) {
}
