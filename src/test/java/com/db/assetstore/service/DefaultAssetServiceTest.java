package com.db.assetstore.service;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.search.Operator;
import com.db.assetstore.domain.search.SearchCriteria;
import com.db.assetstore.domain.service.AssetService;
import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.infra.jpa.AttributeEntity;
import com.db.assetstore.infra.jpa.AttributeHistoryEntity;
import com.db.assetstore.infra.mapper.AssetMapper;
import com.db.assetstore.infra.mapper.AttributeHistoryMapper;
import com.db.assetstore.infra.mapper.AttributeMapper;
import com.db.assetstore.infra.repository.AssetRepository;
import com.db.assetstore.infra.repository.AttributeDefRepository;
import com.db.assetstore.infra.repository.AttributeHistoryRepository;
import com.db.assetstore.infra.repository.AttributeRepository;
import com.db.assetstore.infra.service.search.AssetSearchSpecificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultAssetServiceTest {

    private AssetRepository assetRepo;
    private AttributeRepository attributeRepo;
    private AttributeDefRepository defRepo;
    private AttributeHistoryRepository historyRepo;
    private AssetService service;

    @BeforeEach
    void setup() {
        assetRepo = mock(AssetRepository.class);
        attributeRepo = mock(AttributeRepository.class);
        defRepo = mock(AttributeDefRepository.class);
        historyRepo = mock(AttributeHistoryRepository.class);
        var assetMapper = Mappers.getMapper(AssetMapper.class);
        var attributeMapper = Mockito.mock(AttributeMapper.class,
                Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS));
        var specService = new AssetSearchSpecificationService();
        var historyMapper = Mappers.getMapper(AttributeHistoryMapper.class);
        service = new com.db.assetstore.infra.service.AssetService(assetMapper, attributeMapper, assetRepo, attributeRepo, defRepo, historyRepo, specService, historyMapper);
    }

    @Test
    void addAssetPersistsEntity() {
        Asset asset = new Asset("id1", AssetType.CRE, Instant.now(),
                AttributesCollection.fromMap(Map.of(
                        "city", List.of(new AVString("city", "Warsaw")))));
        when(assetRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        String id = service.addAsset(asset);
        assertEquals("id1", id);
        verify(assetRepo).save(any(AssetEntity.class));
    }

    @Test
    void addAssetFromJsonParsesAndSaves() {
        String json = "{\"type\":\"CRE\",\"id\":\"id2\",\"city\":\"Krakow\"}";
        when(assetRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        String id = service.addAssetFromJson(json);
        assertEquals("id2", id);
        verify(assetRepo).save(any(AssetEntity.class));
    }

    @Test
    void removeAndGetAndHistory() {
        AssetEntity e = AssetEntity.builder().id("Y").type(AssetType.CRE).build();
        e.setDeleted(0);
        e.setAttributes(new ArrayList<AttributeEntity>());
        when(assetRepo.findByIdAndDeleted("Y", 0)).thenReturn(Optional.of(e));
        when(historyRepo.findAllByAsset_IdOrderByChangedAt("Z"))
                .thenReturn(List.of(new AttributeHistoryEntity()));

        assertTrue(service.getAsset("Y").isPresent());
        assertNotNull(service.history("Z"));
    }

    @Test
    void searchWorks() {
        when(assetRepo.findAll(any(Specification.class))).thenReturn(List.of());
        SearchCriteria sc = SearchCriteria.builder()
                .type(AssetType.CRE)
                .where("city", Operator.EQ, new AVString("city", "warsaw")).build();
        assertNotNull(service.search(sc));
        verify(assetRepo).findAll(any(Specification.class));
    }
}
