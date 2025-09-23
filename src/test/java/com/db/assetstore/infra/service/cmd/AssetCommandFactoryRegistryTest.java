package com.db.assetstore.infra.service.cmd;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
import com.db.assetstore.infra.api.dto.AssetPatchRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AssetCommandFactoryRegistryTest {

    @Mock
    private AttributeJsonReader attributeJsonReader;

    private AssetCommandFactoryRegistry registry;

    private AssetCreateRequest createRequest;

    private AssetPatchRequest patchRequest;

    private ObjectNode createAttributes;

    private ObjectNode patchAttributes;

    @BeforeEach
    void setUp() {
        registry = new AssetCommandFactoryRegistry(
                new CreateAssetCommandFactory(attributeJsonReader),
                new PatchAssetCommandFactory(attributeJsonReader)
        );

        createAttributes = new ObjectMapper().createObjectNode();
        createAttributes.put("rating", "A");
        createRequest = new AssetCreateRequest(
                "asset-1",
                AssetType.CRE,
                "ACTIVE",
                "OFFICE",
                null,
                2024,
                "Created",
                "USD",
                createAttributes
        );

        patchAttributes = new ObjectMapper().createObjectNode();
        patchAttributes.put("tenant", "ACME");
        patchRequest = new AssetPatchRequest();
        patchRequest.setStatus("INACTIVE");
        patchRequest.setSubtype("SHOPPING");
        patchRequest.setDescription("Patched");
        patchRequest.setCurrency("EUR");
        patchRequest.setAttributes(patchAttributes);
    }

    @Test
    void createCreateCommand_delegatesToCreateFactory() {
        AttributeValue<?> attribute = AVString.of("rating", "A");
        lenient().when(attributeJsonReader.read(AssetType.CRE, createAttributes)).thenReturn(java.util.List.of(attribute));

        CreateAssetCommand result = registry.createCreateCommand(createRequest);

        assertThat(result.id()).isEqualTo("asset-1");
        assertThat(result.type()).isEqualTo(AssetType.CRE);
        assertThat(result.status()).isEqualTo("ACTIVE");
        assertThat(result.subtype()).isEqualTo("OFFICE");
        assertThat(result.description()).isEqualTo("Created");
        assertThat(result.currency()).isEqualTo("USD");
        assertThat(result.attributes()).containsExactly(attribute);
        verify(attributeJsonReader).read(AssetType.CRE, createAttributes);
    }

    @Test
    void createPatchCommand_withExplicitId_delegatesToPatchFactory() {
        AttributeValue<?> attribute = AVString.of("tenant", "ACME");
        lenient().when(attributeJsonReader.read(AssetType.CRE, patchAttributes)).thenReturn(java.util.List.of(attribute));

        PatchAssetCommand result = registry.createPatchCommand(AssetType.CRE, "asset-2", patchRequest);

        assertThat(result.assetId()).isEqualTo("asset-2");
        assertThat(result.status()).isEqualTo("INACTIVE");
        assertThat(result.subtype()).isEqualTo("SHOPPING");
        assertThat(result.description()).isEqualTo("Patched");
        assertThat(result.currency()).isEqualTo("EUR");
        assertThat(result.attributes()).containsExactly(attribute);
        verify(attributeJsonReader).read(AssetType.CRE, patchAttributes);
    }

    @Test
    void createPatchCommand_usingRequestId_validatesPresence() {
        patchRequest.setId("asset-3");
        AttributeValue<?> attribute = AVString.of("tenant", "ACME");
        lenient().when(attributeJsonReader.read(AssetType.SHIP, patchAttributes)).thenReturn(java.util.List.of(attribute));

        PatchAssetCommand result = registry.createPatchCommand(AssetType.SHIP, patchRequest);

        assertThat(result.assetId()).isEqualTo("asset-3");
        assertThat(result.attributes()).containsExactly(attribute);
    }

    @Test
    void createPatchCommand_withoutRequestId_throwsException() {
        assertThatThrownBy(() -> registry.createPatchCommand(AssetType.CRE, patchRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("asset id");
    }
}
