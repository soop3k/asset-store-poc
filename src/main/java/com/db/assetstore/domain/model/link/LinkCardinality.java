package com.db.assetstore.domain.model.link;

public enum LinkCardinality {
    ASSET_ONE_TARGET_ONE,
    ASSET_MANY_TARGET_ONE,
    ASSET_ONE_TARGET_MANY;

    public boolean limitsAssetSide() {
        return this == ASSET_ONE_TARGET_ONE || this == ASSET_ONE_TARGET_MANY;
    }

    public boolean limitsTargetSide() {
        return this == ASSET_ONE_TARGET_ONE || this == ASSET_MANY_TARGET_ONE;
    }
}
