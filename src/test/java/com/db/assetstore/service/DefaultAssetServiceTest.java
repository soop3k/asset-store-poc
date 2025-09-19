package com.db.assetstore.service;

import com.db.assetstore.AssetType;
import com.db.assetstore.model.Asset;
import com.db.assetstore.model.AttributeHistory;
import com.db.assetstore.model.AttributeValue;
import com.db.assetstore.repo.AssetRepository;
import com.db.assetstore.search.SearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultAssetServiceTest {

    private AssetRepository repository;
    private DefaultAssetService service;

    @BeforeEach
    void setup() {
        repository = mock(AssetRepository.class);
        service = new DefaultAssetService(repository);
    }

    @Test
    void addAssetDelegatesToRepository() {
        Asset asset = new Asset("id1", AssetType.CRE, Instant.now(), List.of(new AttributeValue<>("city", "Warsaw", String.class)));
        when(repository.saveAsset(any())).thenReturn("id1");
        String id = service.addAsset(asset);
        assertEquals("id1", id);
        verify(repository).saveAsset(asset);
    }

    @Test
    void addAssetFromJsonParsesAndSaves() {
        String json = "{\"type\":\"CRE\",\"id\":\"id2\",\"attributes\":{\"city\":\"Krakow\"}}";
        when(repository.saveAsset(any())).thenReturn("id2");
        String id = service.addAssetFromJson(json);
        assertEquals("id2", id);
        ArgumentCaptor<Asset> captor = ArgumentCaptor.forClass(Asset.class);
        verify(repository).saveAsset(captor.capture());
        assertEquals("id2", captor.getValue().getId());
        assertEquals("Krakow", captor.getValue().attributes().get("city").value());
    }

    @Test
    void removeAndGetAndHistoryDelegate() {
        service.removeAsset("X");
        verify(repository).softDelete("X");

        when(repository.findById("Y")).thenReturn(Optional.empty());
        assertTrue(service.getAsset("Y").isEmpty());

        when(repository.history("Z")).thenReturn(List.of(new AttributeHistory("Z","a","1","String", Instant.now())));
        assertEquals(1, service.history("Z").size());
    }

    @Test
    void searchDelegates() {
        SearchCriteria sc = SearchCriteria.builder().type(AssetType.CRE).where("city", com.db.assetstore.search.Operator.EQ, "Warsaw").build();
        when(repository.search(sc)).thenReturn(List.of());
        assertNotNull(service.search(sc));
        verify(repository).search(sc);
    }
}
