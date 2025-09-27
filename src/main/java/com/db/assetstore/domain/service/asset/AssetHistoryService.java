package com.db.assetstore.domain.service.asset;

import com.db.assetstore.domain.model.attribute.AttributeHistory;

import java.util.List;

public interface AssetHistoryService {
    List<AttributeHistory> history(String id);
}
