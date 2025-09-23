package com.db.assetstore.domain.model;

/**
 * Simple identifier wrapper for Asset domain model.
 */
public record AssetId(String id) {
    public static AssetId of(String id) { return new AssetId(id); }
}