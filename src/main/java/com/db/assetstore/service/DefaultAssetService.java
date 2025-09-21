package com.db.assetstore.service;

import lombok.extern.slf4j.Slf4j;
import com.db.assetstore.json.AssetJsonFactory;
import com.db.assetstore.service.validation.AssetAttributeValidationService;
import com.db.assetstore.AssetType;
import com.db.assetstore.model.Asset;
import com.db.assetstore.model.AttributeHistory;
import com.db.assetstore.repository.AssetRepository;
import com.db.assetstore.search.SearchCriteria;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@Slf4j
public final class DefaultAssetService implements AssetService {
    private final AssetRepository repository;
    private final AssetJsonFactory assetJsonFactory = new AssetJsonFactory();
    private final AssetAttributeValidationService validationService = new AssetAttributeValidationService();
    private final ObjectMapper mapper = new ObjectMapper();

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
        Objects.requireNonNull(json, "json");
        // Parse & validate without exposing JsonNode to callers
        validationService.validateEnvelope(json);
        Asset asset = assetJsonFactory.fromJson(json);
        return addAsset(asset);
    }

    @Override
    public String addAssetFromJson(AssetType type, String json) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(json, "json");
        validationService.validateForType(type, json);
        Asset asset = assetJsonFactory.fromJsonForType(type, json);
        return addAsset(asset);
    }

    @Override
    public List<String> addAssetsFromJsonArray(String jsonArray) {
        Objects.requireNonNull(jsonArray, "jsonArray");
        try {
            JsonNode node = mapper.readTree(jsonArray);
            if (node == null || !node.isArray()) {
                throw new IllegalArgumentException("Expected JSON array of assets");
            }
            List<String> ids = new ArrayList<>();
            for (JsonNode item : node) {
                String itemJson = mapper.writeValueAsString(item);
                validationService.validateEnvelope(itemJson);
                Asset asset = assetJsonFactory.fromJson(itemJson);
                String id = addAsset(asset);
                ids.add(id);
            }
            return ids;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON array payload: " + e.getMessage(), e);
        }
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
