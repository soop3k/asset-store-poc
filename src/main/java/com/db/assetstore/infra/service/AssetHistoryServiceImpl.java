package com.db.assetstore.infra.service;

import com.db.assetstore.domain.model.asset.AssetHistory;
import com.db.assetstore.domain.model.attribute.AttributeHistory;
import com.db.assetstore.domain.service.asset.AssetHistoryService;
import com.db.assetstore.infra.jpa.AssetHistoryEntity;
import com.db.assetstore.infra.jpa.AttributeHistoryEntity;
import com.db.assetstore.infra.mapper.AssetHistoryMapper;
import com.db.assetstore.infra.mapper.AttributeHistoryMapper;
import com.db.assetstore.infra.repository.AssetHistoryRepository;
import com.db.assetstore.infra.repository.AttributeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetHistoryServiceImpl implements AssetHistoryService {

    private final AttributeHistoryRepository attributeHistoryRepository;
    private final AttributeHistoryMapper attributeHistoryMapper;
    private final AssetHistoryRepository assetHistoryRepository;
    private final AssetHistoryMapper assetHistoryMapper;


    @Override
    @Transactional(readOnly = true)
    public List<AttributeHistory> history(String id) {
        return attributeHistory(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributeHistory> attributeHistory(String id) {
        List<AttributeHistoryEntity> rows =
                attributeHistoryRepository.findAllByAsset_IdOrderByChangedAt(id);
        return attributeHistoryMapper.toModels(rows);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetHistory> assetHistory(String id) {
        List<AssetHistoryEntity> rows = assetHistoryRepository.findAllByAsset_IdOrderByChangedAt(id);
        return assetHistoryMapper.toModels(rows);
    }
}
