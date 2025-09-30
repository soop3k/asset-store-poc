package com.db.assetstore.service;

import com.db.assetstore.domain.model.asset.AssetHistory;
import com.db.assetstore.domain.model.attribute.AttributeHistory;
import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.infra.jpa.AttributeEntity;
import com.db.assetstore.infra.jpa.AttributeHistoryEntity;
import com.db.assetstore.infra.jpa.AssetHistoryEntity;
import com.db.assetstore.infra.mapper.AssetHistoryMapper;
import com.db.assetstore.infra.mapper.AttributeHistoryMapper;
import com.db.assetstore.infra.repository.AssetHistoryRepository;
import com.db.assetstore.infra.repository.AttributeHistoryRepository;
import com.db.assetstore.infra.service.AssetHistoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssetHistoryServiceTest {

    AttributeHistoryRepository historyRepo;
    AttributeHistoryMapper historyMapper;
    AssetHistoryRepository assetHistoryRepository;
    AssetHistoryMapper assetHistoryMapper;

    AssetHistoryServiceImpl service;

    @BeforeEach
    void setup() {
        historyRepo = mock(AttributeHistoryRepository.class);
        historyMapper = Mappers.getMapper(AttributeHistoryMapper.class);
        assetHistoryRepository = mock(AssetHistoryRepository.class);
        assetHistoryMapper = Mappers.getMapper(AssetHistoryMapper.class);
        service = new AssetHistoryServiceImpl(historyRepo, historyMapper, assetHistoryRepository, assetHistoryMapper);
    }

    @Test
    void attributeHistoryForAsset() {
        // given
        AssetEntity asset = AssetEntity.builder().id("h-1").build();
        AttributeEntity attr = new AttributeEntity(asset, "city", "Gdansk", Instant.now());

        AttributeHistoryEntity h1 = new AttributeHistoryEntity(attr, Instant.now().minusSeconds(10));
        AttributeHistoryEntity h2 = new AttributeHistoryEntity(attr, Instant.now());
        when(historyRepo.findAllByAsset_IdOrderByChangedAt("h-1")).thenReturn(List.of(h1, h2));

        // when
        List<AttributeHistory> out = service.attributeHistory("h-1");

        // then (default mapper turns entities into domain model)
        assertEquals(2, out.size());
        assertEquals("city", out.get(0).name());
        assertEquals("Gdansk", out.get(0).valueStr());
        assertTrue(out.get(0).changedAt().isBefore(out.get(1).changedAt()));
    }

    @Test
    void assetHistoryForAsset() {
        // given
        AssetEntity asset = AssetEntity.builder()
                .id("h-asset")
                .status("NEW")
                .currency("USD")
                .description("Initial")
                .modifiedAt(Instant.now().minusSeconds(5))
                .modifiedBy("tester")
                .createdAt(Instant.now().minusSeconds(10))
                .createdBy("creator")
                .build();
        AssetHistoryEntity h1 = new AssetHistoryEntity(asset, Instant.now());
        when(assetHistoryRepository.findAllByAsset_IdOrderByChangedAt("h-asset"))
                .thenReturn(List.of(h1));

        // when
        List<AssetHistory> out = service.assetHistory("h-asset");

        // then
        assertEquals(1, out.size());
        AssetHistory history = out.get(0);
        assertEquals("NEW", history.status());
        assertEquals("USD", history.currency());
        assertEquals("creator", history.createdBy());
        assertFalse(history.deleted());
        assertNotNull(history.changedAt());
    }
}
