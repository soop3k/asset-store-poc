package com.db.assetstore.domain.service;

import com.db.assetstore.domain.model.attribute.AttributeHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface AssetHistoryService {
    List<AttributeHistory> history(String id);
}
