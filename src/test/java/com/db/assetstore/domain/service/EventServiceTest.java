package com.db.assetstore.domain.service;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.exception.EventGenerationException;
import com.db.assetstore.domain.exception.JsonTransformException;
import com.db.assetstore.domain.json.AssetCanonicalizer;
import com.db.assetstore.domain.model.Asset;
import com.db.assetstore.domain.service.transform.JsonTransformer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventServiceTest {

    JsonTransformer transformer;
    AssetCanonicalizer canonicalizer;
    ObjectMapper objectMapper;
    EventService service;

    @BeforeEach
    void setUp() {
        transformer = mock(JsonTransformer.class);
        canonicalizer = mock(AssetCanonicalizer.class);
        objectMapper = new ObjectMapper();
        service = new EventService(transformer, canonicalizer, objectMapper);
    }

    @Test
    void generate_whenCanonicalJsonCannotBeParsed_throwsEventGenerationException() throws Exception {
        Asset asset = Asset.builder()
                .id("asset-1")
                .type(AssetType.CRE)
                .createdAt(Instant.now())
                .build();
        asset.setAttributes(List.of());

        when(canonicalizer.toCanonicalJson(any())).thenReturn("not-json");

        assertThatThrownBy(() -> service.generate("asset-cre", asset))
                .isInstanceOf(EventGenerationException.class)
                .hasMessageContaining("asset-1")
                .hasMessageContaining("canonical asset JSON");
    }

    @Test
    void generate_whenTransformerThrows_propagatesJsonTransformException() throws Exception {
        Asset asset = Asset.builder()
                .id("asset-1")
                .type(AssetType.CRE)
                .createdAt(Instant.now())
                .build();
        asset.setAttributes(List.of());

        when(canonicalizer.toCanonicalJson(any())).thenReturn("{\"id\":\"asset-1\"}");
        when(transformer.transform(any(), any())).thenThrow(new JsonTransformException("boom"));

        assertThatThrownBy(() -> service.generate("asset-cre", asset))
                .isInstanceOf(JsonTransformException.class)
                .hasMessageContaining("boom");
    }
}
