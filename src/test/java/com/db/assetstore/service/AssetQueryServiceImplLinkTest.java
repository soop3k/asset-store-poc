package com.db.assetstore.service;

import com.db.assetstore.domain.model.link.AssetLink;
import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.infra.jpa.link.AssetLinkEntity;
import com.db.assetstore.infra.mapper.AssetLinkMapper;
import com.db.assetstore.infra.mapper.AssetMapper;
import com.db.assetstore.infra.repository.AssetRepository;
import com.db.assetstore.infra.repository.link.AssetLinkRepository;
import com.db.assetstore.infra.service.AssetQueryServiceImpl;
import com.db.assetstore.infra.service.search.AssetSearchSpecificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AssetQueryServiceImplLinkTest {

    AssetRepository assetRepository;
    AssetLinkRepository assetLinkRepository;
    AssetSearchSpecificationService specService;
    AssetMapper assetMapper;
    AssetLinkMapper assetLinkMapper;

    AssetQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        assetRepository = mock(AssetRepository.class);
        assetLinkRepository = mock(AssetLinkRepository.class);
        specService = mock(AssetSearchSpecificationService.class);
        assetMapper = mock(AssetMapper.class);
        assetLinkMapper = spy(new AssetLinkMapper());
        when(specService.buildSpec(any())).thenReturn((Specification<AssetEntity>) (root, query, cb) -> cb.conjunction());
        service = new AssetQueryServiceImpl(assetMapper, assetRepository, specService, assetLinkRepository, assetLinkMapper);
    }

    @Test
    void findLinksByAsset_returnsMappedLinks() {
        AssetLinkEntity entity = AssetLinkEntity.builder()
                .id("L1")
                .assetId("A1")
                .linkCode("WORKFLOW")
                .linkSubtype("BULK")
                .entityType("WORKFLOW")
                .entityId("WF-1")
                .active(true)
                .deleted(false)
                .validFrom(Instant.now())
                .build();
        when(assetLinkRepository.findByAssetIdAndDeleted("A1", false)).thenReturn(List.of(entity));

        List<AssetLink> links = service.findLinksByAsset("A1", false);

        assertEquals(1, links.size());
        assertEquals("L1", links.get(0).getId());
    }

    @Test
    void findLinksByEntity_returnsMappedLinks() {
        AssetLinkEntity entity = AssetLinkEntity.builder()
                .id("L2")
                .assetId("A1")
                .linkCode("WORKFLOW")
                .linkSubtype("BULK")
                .entityType("WORKFLOW")
                .entityId("WF-2")
                .active(true)
                .deleted(false)
                .validFrom(Instant.now())
                .build();
        when(assetLinkRepository.findByEntityTypeAndEntityIdAndDeleted("WORKFLOW", "WF-2", false)).thenReturn(List.of(entity));

        List<AssetLink> links = service.findLinksByEntity("WORKFLOW", "WF-2", false);

        assertEquals(1, links.size());
        assertEquals("L2", links.get(0).getId());
    }
}
