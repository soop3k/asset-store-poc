package com.db.assetstore.service;

import com.db.assetstore.model.AttributeHistory;
import com.db.assetstore.model.Asset;
import com.db.assetstore.search.SearchCriteria;

import java.util.List;
import java.util.Optional;

public interface AssetService {
    String addAsset(Asset asset);
    /**
     * Create asset from JSON payload with structure like:
     * {"type":"CRE","id":"optional-uuid","attributes":{"city":"Warsaw","area":100.0}}
     */
    String addAssetFromJson(String json);

    /**
     * Create asset from a type-specific JSON (no generic wrapper). The payload is validated against
     * a JSON schema defined per type and then converted to an Asset. All top-level fields except 'id'
     * become attributes.
     */
    String addAssetFromJson(com.db.assetstore.AssetType type, String json);

    void removeAsset(String assetId);
    Optional<Asset> getAsset(String assetId);

    List<Asset> search(SearchCriteria criteria);
    List<AttributeHistory> history(String assetId);
}
