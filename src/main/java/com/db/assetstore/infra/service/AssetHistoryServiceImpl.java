package com.db.assetstore.infra.service;

import com.db.assetstore.domain.model.AssetId;
import com.db.assetstore.domain.model.attribute.AttributeHistory;
import com.db.assetstore.domain.service.AssetHistoryService;
import com.db.assetstore.infra.mapper.AttributeHistoryMapper;
import com.db.assetstore.infra.repository.AttributeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
    public Page<AttributeHistory> history(AssetId id, PageRequest page) {
        List<com.db.assetstore.infra.jpa.AttributeHistoryEntity> rows =
                historyRepo.findAllByAsset_IdOrderByChangedAt(id.id());
        List<AttributeHistory> all = new ArrayList<>(rows.size());
        for (var h : rows) {
            all.add(historyMapper.toModel(h));
        }
        int offset = (int) page.getOffset();
        int size = page.getPageSize();
        if (offset >= all.size()) {
            return new PageImpl<>(List.of(), page, all.size());
        }
        int toIndex = Math.min(offset + size, all.size());
        List<AttributeHistory> content = all.subList(offset, toIndex);
        return new PageImpl<>(content, page, all.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributeHistory> history(AssetId id) {
        List<com.db.assetstore.infra.jpa.AttributeHistoryEntity> rows =
                historyRepo.findAllByAsset_IdOrderByChangedAt(id.id());
        List<AttributeHistory> all = new ArrayList<>(rows.size());
        for (var h : rows) {
            all.add(historyMapper.toModel(h));
        }
        return all;
    }
}
