package com.db.assetstore.domain.service.asset;

import com.db.assetstore.domain.model.asset.AssetHistory;
import com.db.assetstore.domain.model.attribute.AttributeHistory;

import java.util.List;

public interface AssetHistoryService {
    List<AttributeHistory> attributeHistory(String id);

    default List<AttributeHistory> history(String id) {
        return attributeHistory(id);
    }

    List<AssetHistory> assetHistory(String id);
}
