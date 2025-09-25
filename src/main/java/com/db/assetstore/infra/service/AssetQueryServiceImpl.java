package com.db.assetstore.infra.service;

import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.service.AssetQueryService;
import com.db.assetstore.domain.search.SearchCriteria;
import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.infra.mapper.AssetMapper;
import com.db.assetstore.infra.repository.AssetRepository;
import com.db.assetstore.infra.service.search.AssetSearchSpecificationService;
import com.db.assetstore.domain.service.link.AssetLinkQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssetQueryServiceImpl implements AssetQueryService {

    private final AssetMapper assetMapper;
    private final AssetRepository assetRepo;
    private final AssetSearchSpecificationService specService;
    private final AssetLinkQueryService assetLinkQueryService;

    @Override
    @Transactional(readOnly = true)
    public Optional<Asset> get(String id) {
        return assetRepo.findByIdAndDeleted(id, 0)
                .map(assetMapper::toModel)
                .map(asset -> enrichWithLinks(id, asset));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Asset> search(SearchCriteria criteria) {
        List<AssetEntity> entities = searchEntities(criteria);
        List<Asset> assets = assetMapper.toModelList(entities);
        assets.forEach(asset -> asset.setLinks(assetLinkQueryService.findActiveLinks(asset.getId())));
        return assets;
    }

    private List<AssetEntity> searchEntities(SearchCriteria criteria) {
        return assetRepo.findAll(specService.buildSpec(criteria));
    }

    private Asset enrichWithLinks(String assetId, Asset asset) {
        asset.setLinks(assetLinkQueryService.findActiveLinks(assetId));
        return asset;
    }
}
