package com.db.assetstore.service;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.AssetId;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.search.SearchCriteria;
import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.infra.jpa.AttributeEntity;
import com.db.assetstore.infra.mapper.AssetMapper;
import com.db.assetstore.infra.mapper.AttributeMapper;
import com.db.assetstore.infra.repository.AssetRepository;
import com.db.assetstore.infra.service.AssetQueryServiceImpl;
import com.db.assetstore.infra.service.search.AssetSearchSpecificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AssetQueryServiceImplTest {

    AssetMapper assetMapper;
    AttributeMapper attributeMapper;
    AssetRepository assetRepo;
    AssetSearchSpecificationService specService;

    AssetQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        assetMapper = mock(AssetMapper.class);
        attributeMapper = mock(AttributeMapper.class, withSettings().defaultAnswer(org.mockito.Mockito.CALLS_REAL_METHODS));
        assetRepo = mock(AssetRepository.class);
        specService = mock(AssetSearchSpecificationService.class);
        service = new AssetQueryServiceImpl(assetMapper, attributeMapper, assetRepo, specService);
        when(specService.buildSpec(any())).thenReturn((org.springframework.data.jpa.domain.Specification) (root, query, cb) -> cb.conjunction());
    }

    @Test
    void get_returnsMappedAsset_andPopulatesAttributesWhenMapperReturnsEmpty() {
        // given entity with one attribute
        AssetEntity e = AssetEntity.builder().id("q-1").type(AssetType.CRE)
                .status("ACTIVE")
                .description("desc")
                .attributes(new ArrayList<>())
                .build();
        AttributeEntity attr = new AttributeEntity(e, "city", "Warsaw", Instant.now());
        e.getAttributes().add(attr);
        when(assetRepo.findByIdAndDeleted("q-1", 0)).thenReturn(Optional.of(e));

        // mapper returns model with no attributes (so service must populate from entity)
        Asset m = new Asset("q-1", AssetType.CRE, Instant.now(), AttributesCollection.empty());
        when(assetMapper.toModel(e)).thenReturn(m);

        // when
        Asset found = service.get(new AssetId("q-1")).orElseThrow();

        // then simple fields are copied
        assertEquals("ACTIVE", found.getStatus());
        assertEquals("desc", found.getDescription());
        // attributes populated from entity via attributeMapper
        List<AttributeValue<?>> attrs = found.getAttributesFlat();
        assertEquals(1, attrs.size());
        assertTrue(attrs.get(0) instanceof AVString);
        assertEquals("city", attrs.get(0).name());
        assertEquals("Warsaw", ((AVString) attrs.get(0)).value());
    }

    @Test
    void search_nonPaged_returnsMappedList() {
        AssetEntity e1 = AssetEntity.builder().id("s-1").type(AssetType.CRE).attributes(new ArrayList<>()).build();
        AssetEntity e2 = AssetEntity.builder().id("s-2").type(AssetType.SHIP).attributes(new ArrayList<>()).build();
        when(assetRepo.findAll(any(org.springframework.data.jpa.domain.Specification.class))).thenReturn(List.of(e1, e2));

        when(assetMapper.toModel(e1)).thenReturn(new Asset("s-1", AssetType.CRE, Instant.now(), AttributesCollection.empty()));
        when(assetMapper.toModel(e2)).thenReturn(new Asset("s-2", AssetType.SHIP, Instant.now(), AttributesCollection.empty()));

        List<Asset> result = service.search(SearchCriteria.builder().build());
        assertEquals(2, result.size());
        assertEquals("s-1", result.get(0).getId());
        assertEquals("s-2", result.get(1).getId());
    }
}
