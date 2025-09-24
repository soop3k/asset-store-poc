package com.db.assetstore.infra.api.dto;

/**
 * HTTP request payload for deleting an asset.
 */
public record AssetDeleteRequest(
        String id,
        String executedBy
) {
}
