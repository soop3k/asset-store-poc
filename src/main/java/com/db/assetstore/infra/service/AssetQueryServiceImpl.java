package com.db.assetstore.infra.service;

import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.AssetId;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.service.AssetQueryService;
import com.db.assetstore.domain.search.SearchCriteria;
import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.infra.mapper.AssetMapper;
import com.db.assetstore.infra.mapper.AttributeMapper;
import com.db.assetstore.infra.repository.AssetRepository;
import com.db.assetstore.infra.service.search.AssetSearchSpecificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssetQueryServiceImpl implements AssetQueryService {

    private final AssetMapper assetMapper;
    private final AttributeMapper attributeMapper;
    private final AssetRepository assetRepo;
    private final AssetSearchSpecificationService specService;

    @Override
    @Transactional(readOnly = true)
    public Optional<Asset> get(AssetId id) {
        return assetRepo.findByIdAndDeleted(id.id(), 0).map(this::toModelWithFixup);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Asset> search(SearchCriteria criteria, PageRequest page) {
        List<AssetEntity> entities = searchEntities(criteria);
        List<Asset> all = new ArrayList<>(entities.size());
        for (AssetEntity e : entities) {
            all.add(toModelWithFixup(e));
        }
        int offset = (int) page.getOffset();
        int size = page.getPageSize();
        if (offset >= all.size()) {
            return new PageImpl<>(List.of(), page, all.size());
        }
        int toIndex = Math.min(offset + size, all.size());
        List<Asset> content = all.subList(offset, toIndex);
        return new PageImpl<>(content, page, all.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Asset> search(SearchCriteria criteria) {
        List<AssetEntity> entities = searchEntities(criteria);
        List<Asset> all = new ArrayList<>(entities.size());
        for (AssetEntity e : entities) {
            all.add(toModelWithFixup(e));
        }
        return all;
    }

    private List<AssetEntity> searchEntities(SearchCriteria criteria) {
        return assetRepo.findAll(specService.buildSpec(criteria));
    }

    private Asset toModelWithFixup(AssetEntity e) {
        Asset m = assetMapper.toModel(e);
        copySimpleFields(e, m);
        if ((m.getAttributesFlat() == null || m.getAttributesFlat().isEmpty())
                && e.getAttributes() != null && !e.getAttributes().isEmpty()) {
            List<AttributeValue<?>> attrs = new ArrayList<>(e.getAttributes().size());
            e.getAttributes().forEach(a -> attrs.add(attributeMapper.toModel(a)));
            m.setAttributes(attrs);
        }
        return m;
    }

    private static void copySimpleFields(AssetEntity e, Asset m) {
        if (e == null || m == null) return;
        m.setVersion(e.getVersion());
        m.setStatus(e.getStatus());
        m.setSubtype(e.getSubtype());
        m.setStatusEffectiveTime(e.getStatusEffectiveTime());
        m.setCreatedBy(e.getCreatedBy());
        m.setModifiedAt(e.getModifiedAt());
        m.setModifiedBy(e.getModifiedBy());
        m.setNotionalAmount(e.getNotionalAmount());
        m.setYear(e.getYear());
        m.setWh(e.getWh());
        m.setSourceSystemName(e.getSourceSystemName());
        m.setExternalReference(e.getExternalReference());
        m.setDescription(e.getDescription());
        m.setCurrency(e.getCurrency());
    }
}
