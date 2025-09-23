package com.db.assetstore.domain.service;

import com.db.assetstore.domain.model.AssetId;
import com.db.assetstore.domain.model.attribute.AttributeHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface AssetHistoryService {
    Page<AttributeHistory> history(AssetId id, PageRequest page);
    List<AttributeHistory> history(AssetId id);
}
