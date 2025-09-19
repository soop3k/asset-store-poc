package com.db.assetstore.repo;

import com.db.assetstore.model.Asset;
import com.db.assetstore.model.AttributeHistory;
import com.db.assetstore.model.AttributeValue;
import com.db.assetstore.search.SearchCriteria;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AssetRepository {
    String saveAsset(Asset asset);
    void softDelete(String assetId);
    Optional<Asset> findById(String assetId);
    void setAttributes(String assetId, Collection<AttributeValue<?>> attributes);

    List<Asset> search(SearchCriteria criteria);
    List<AttributeHistory> history(String assetId);
}
