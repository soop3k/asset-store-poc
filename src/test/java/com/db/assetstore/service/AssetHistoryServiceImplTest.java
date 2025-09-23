package com.db.assetstore.service;

import com.db.assetstore.domain.model.AssetId;
import com.db.assetstore.domain.model.attribute.AttributeHistory;
import com.db.assetstore.domain.model.type.AttributeType;
import com.db.assetstore.infra.jpa.AssetEntity;
import com.db.assetstore.infra.jpa.AttributeEntity;
import com.db.assetstore.infra.jpa.AttributeHistoryEntity;
import com.db.assetstore.infra.mapper.AttributeHistoryMapper;
import com.db.assetstore.infra.repository.AttributeHistoryRepository;
import com.db.assetstore.infra.service.AssetHistoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssetHistoryServiceImplTest {

    AttributeHistoryRepository historyRepo;
    AttributeHistoryMapper historyMapper;

    AssetHistoryServiceImpl service;

    @BeforeEach
    void setup() {
        historyRepo = mock(AttributeHistoryRepository.class);
        historyMapper = mock(AttributeHistoryMapper.class, withSettings().defaultAnswer(org.mockito.Mockito.CALLS_REAL_METHODS));
        service = new AssetHistoryServiceImpl(historyRepo, historyMapper);
    }

    @Test
    void history_mapsEntitiesToModels_andPreservesOrder() {
        // given
        AssetEntity asset = AssetEntity.builder().id("h-1").build();
        AttributeEntity attr = new AttributeEntity();
        attr.setAsset(asset);
        attr.setName("city");
        attr.setValueType(AttributeType.STRING);
        attr.setValueStr("Gdansk");

        AttributeHistoryEntity h1 = new AttributeHistoryEntity(attr, Instant.now().minusSeconds(10));
        AttributeHistoryEntity h2 = new AttributeHistoryEntity(attr, Instant.now());
        when(historyRepo.findAllByAsset_IdOrderByChangedAt("h-1")).thenReturn(List.of(h1, h2));

        // when
        List<AttributeHistory> out = service.history(new AssetId("h-1"));

        // then (default mapper turns entities into domain model)
        assertEquals(2, out.size());
        assertEquals("city", out.get(0).name());
        assertEquals("Gdansk", out.get(0).valueStr());
        assertTrue(out.get(0).changedAt().isBefore(out.get(1).changedAt()));
    }
}
