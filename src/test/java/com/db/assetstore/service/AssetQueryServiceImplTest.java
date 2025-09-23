package com.db.assetstore.service;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.search.SearchCriteria;
import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.infra.jpa.AttributeEntity;
import com.db.assetstore.infra.mapper.AssetMapper;
import com.db.assetstore.infra.repository.AssetRepository;
import com.db.assetstore.infra.service.AssetQueryServiceImpl;
import com.db.assetstore.infra.service.search.AssetSearchSpecificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AssetQueryServiceImplTest {

    AssetMapper assetMapper = Mappers.getMapper(AssetMapper.class);
    AssetRepository assetRepo;
    AssetSearchSpecificationService specService;

    AssetQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        assetRepo = mock(AssetRepository.class);
        specService = mock(AssetSearchSpecificationService.class);
        service = new AssetQueryServiceImpl(assetMapper, assetRepo, specService);
        when(specService.buildSpec(any())).thenReturn(
                (Specification) (root, query, cb) -> cb.conjunction());
    }

    @Test
    void get_returnsMappedAsset_andPopulatesAttributesWhenMapperReturnsEmpty() {
        // given entity with one attribute
        AssetEntity e = AssetEntity.builder()
                .id("q-1")
                .type(AssetType.CRE)
                .status("ACTIVE")
                .description("desc")
                .attributes(new ArrayList<>())
                .build();
        AttributeEntity attr = new AttributeEntity(e, "city", "Warsaw", Instant.now());
        e.getAttributes().add(attr);
        when(assetRepo.findByIdAndDeleted("q-1", 0)).thenReturn(Optional.of(e));

        // when
        Asset found = service.get("q-1").orElseThrow();

        // then
        assertEquals("q-1", found.getId());
        assertEquals(AssetType.CRE, found.getType());
        assertEquals("ACTIVE", found.getStatus());
        assertEquals("desc", found.getDescription());
        List<AttributeValue<?>> attrs = found.getAttributesFlat();
        assertEquals(1, attrs.size());
        assertTrue(attrs.get(0) instanceof AVString);
        assertEquals("city", attrs.get(0).name());
        assertEquals("Warsaw", ((AVString) attrs.get(0)).value());
    }

    @Test
    void search_nonPaged_returnsMappedList() {
        AssetEntity e1 = AssetEntity.builder()
                .id("s-1")
                .type(AssetType.CRE)
                .attributes(new ArrayList<>())
                .build();
        AssetEntity e2 = AssetEntity.builder()
                .id("s-2")
                .type(AssetType.SHIP)
                .attributes(new ArrayList<>())
                .build();
        when(assetRepo.findAll(any(Specification.class))).thenReturn(List.of(e1, e2));

        // when
        List<Asset> result = service.search(SearchCriteria.builder().build());

        // then
        assertEquals(2, result.size());
        assertEquals("s-1", result.get(0).getId());
        assertEquals("s-2", result.get(1).getId());
    }
}
