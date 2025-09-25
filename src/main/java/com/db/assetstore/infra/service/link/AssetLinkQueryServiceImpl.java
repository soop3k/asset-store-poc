package com.db.assetstore.infra.service.link;

import com.db.assetstore.domain.model.link.AssetLink;
import com.db.assetstore.domain.service.link.AssetLinkQueryService;
import com.db.assetstore.infra.jpa.AssetLinkEntity;
import com.db.assetstore.infra.mapper.AssetLinkMapper;
import com.db.assetstore.infra.repository.AssetLinkRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetLinkQueryServiceImpl implements AssetLinkQueryService {

    private final AssetLinkRepo assetLinkRepo;
    private final AssetLinkMapper assetLinkMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AssetLink> findActiveLinks(String assetId) {
        return toModelList(assetLinkRepo.activeForAsset(assetId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetLink> findLinks(String assetId, boolean includeInactive) {
        if (includeInactive) {
            return toModelList(assetLinkRepo.allForAsset(assetId));
        }
        return findActiveLinks(assetId);
    }

    private List<AssetLink> toModelList(List<AssetLinkEntity> entities) {
        List<AssetLink> mapped = assetLinkMapper.toModelList(entities);
        return mapped == null ? List.of() : List.copyOf(mapped);
    }
}
