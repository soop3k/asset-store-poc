package com.db.assetstore.domain.service;

import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.AssetId;
import com.db.assetstore.domain.model.AssetPatch;
import com.db.assetstore.domain.model.attribute.AttributeValue;

import java.util.Collection;

public interface AssetCommandService {
    // Preferred command-based API
    AssetId create(CreateAssetCommand command);
    void update(PatchAssetCommand command);

    // Backward-compatible API (to be removed later)
    AssetId create(Asset asset);
    void updateAttributes(AssetId id, Collection<AttributeValue<?>> attrs);
    void update(AssetId id, AssetPatch patch);
    void delete(AssetId id);
}
