package com.db.assetstore.domain.service.asset;

import com.db.assetstore.domain.model.asset.Asset;
import com.db.assetstore.domain.search.SearchCriteria;

import java.util.List;
import java.util.Optional;

public interface AssetQueryService {
    Optional<Asset> get(String id);
    List<Asset> search(SearchCriteria criteria);
}
