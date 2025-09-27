package com.db.assetstore.infra.service;

import com.db.assetstore.domain.model.attribute.AttributeHistory;
import com.db.assetstore.domain.service.asset.AssetHistoryService;
import com.db.assetstore.infra.mapper.AttributeHistoryMapper;
import com.db.assetstore.infra.repository.AttributeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetHistoryServiceImpl implements AssetHistoryService {

    private final AttributeHistoryRepository historyRepo;
    private final AttributeHistoryMapper historyMapper;


    @Override
    @Transactional(readOnly = true)
    public List<AttributeHistory> history(String id) {
        List<com.db.assetstore.infra.jpa.AttributeHistoryEntity> rows =
                historyRepo.findAllByAsset_IdOrderByChangedAt(id);
        List<AttributeHistory> all = new ArrayList<>(rows.size());
        for (var h : rows) {
            all.add(historyMapper.toModel(h));
        }
        return all;
    }
}
