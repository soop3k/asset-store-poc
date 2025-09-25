package com.db.assetstore.domain.model.link;

/**
 * Describes allowed number of active links between an Asset and external entity instances.
 */
public enum LinkCardinality {
    ONE_TO_ONE,
    ONE_TO_MANY,
    MANY_TO_ONE;

    public boolean limitsAssetSide() {
        return this == ONE_TO_ONE || this == MANY_TO_ONE;
    }

    public boolean limitsTargetSide() {
        return this == ONE_TO_ONE || this == ONE_TO_MANY;
    }
}
