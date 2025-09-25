package com.db.assetstore.infra.api.dto;

/**
 * HTTP payload for deleting (deactivating) an existing Asset link.
 */
public record AssetLinkDeleteRequest(
        String entityType,
        String entitySubtype,
        String targetCode,
        String executedBy
) {
}
