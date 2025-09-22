package com.db.assetstore.domain.service;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.attribute.AttributeHistory;
import com.db.assetstore.domain.search.SearchCriteria;

import java.util.List;
import java.util.Optional;

/**
 * Domain-level AssetService contract. Implementations may live in infra layer.
 * This interface must not depend on infra packages.
 */
public interface AssetService {
    String addAsset(Asset asset);
    String addAssetFromJson(String json);
    String addAssetFromJson(AssetType type, String json);
    List<String> addAssetsFromJsonArray(String jsonArray);
    Optional<Asset> getAsset(String assetId);
    List<Asset> search(SearchCriteria criteria);
    List<AttributeHistory> history(String assetId);
    void updateAssetFromJson(String assetId, String json);
}
