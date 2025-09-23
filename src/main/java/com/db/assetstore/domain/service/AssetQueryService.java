package com.db.assetstore.domain.service;

import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.search.SearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

public interface AssetQueryService {
    Optional<Asset> get(String id);
    List<Asset> search(SearchCriteria criteria);
}
