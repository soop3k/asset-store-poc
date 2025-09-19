package com.db.assetstore.service;

import lombok.extern.slf4j.Slf4j;
import com.db.assetstore.model.Asset;
import com.db.assetstore.model.AttributeHistory;
import com.db.assetstore.repo.AssetRepository;
import com.db.assetstore.search.SearchCriteria;

import java.util.*;

@Slf4j
public final class DefaultAssetService implements AssetService {
    private final AssetRepository repository;
    private final AssetJsonFactory assetJsonFactory = new AssetJsonFactory();

    public DefaultAssetService(AssetRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public String addAsset(Asset asset) {
        log.info("Adding asset: type={}, id={}", asset.getType(), asset.getId());
        return repository.saveAsset(asset);
    }

    @Override
    public String addAssetFromJson(String json) {
        Asset asset = assetJsonFactory.fromJson(json);
        return addAsset(asset);
    }

    @Override
    public String addAssetFromJson(com.db.assetstore.AssetType type, String json) {
        Asset asset = assetJsonFactory.fromJsonForType(type, json);
        return addAsset(asset);
    }

    @Override
    public void removeAsset(String assetId) {
        repository.softDelete(assetId);
    }

    @Override
    public Optional<Asset> getAsset(String assetId) {
        return repository.findById(assetId);
    }

    @Override
    public List<Asset> search(SearchCriteria criteria) {
        return repository.search(criteria);
    }

    @Override
    public List<AttributeHistory> history(String assetId) {
        return repository.history(assetId);
    }
}
