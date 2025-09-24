package com.db.assetstore.infra.service.cmd;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;
import com.db.assetstore.domain.service.cmd.factory.AssetCommandFactoryRegistry;
import com.db.assetstore.domain.service.cmd.factory.CreateAssetCommandFactory;
import com.db.assetstore.domain.service.cmd.factory.PatchAssetCommandFactory;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.db.assetstore.domain.service.type.TypeSchemaRegistry;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
import com.db.assetstore.infra.api.dto.AssetPatchRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssetCommandFactoryRegistryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AssetCommandFactoryRegistry registry;

    private AssetCreateRequest createRequest;

    private AssetPatchRequest patchRequest;

    private ObjectNode createAttributes;

    private ObjectNode patchAttributes;

    @BeforeEach
    void setUp() {
        AttributeJsonReader attributeJsonReader = createJsonReader();
        registry = new AssetCommandFactoryRegistry(
                new CreateAssetCommandFactory(attributeJsonReader),
                new PatchAssetCommandFactory(attributeJsonReader)
        );

        createAttributes = objectMapper.createObjectNode();
        createAttributes.put("city", "Frankfurt");
        createAttributes.put("area", new BigDecimal("500.25"));
        createAttributes.put("active", true);
        createRequest = new AssetCreateRequest(
                "asset-1",
                AssetType.CRE,
                "ACTIVE",
                "OFFICE",
                new BigDecimal("150.00"),
                2024,
                "Created",
                "USD",
                createAttributes
        );

        patchAttributes = objectMapper.createObjectNode();
        patchAttributes.put("name", "Sea Queen");
        patchAttributes.put("imo", 9876543);
        patchAttributes.put("active", false);
        patchRequest = new AssetPatchRequest();
        patchRequest.setStatus("INACTIVE");
        patchRequest.setSubtype("FREIGHT");
        patchRequest.setDescription("Patched");
        patchRequest.setCurrency("EUR");
        patchRequest.setAttributes(patchAttributes);
    }

    @Test
    void createCreateCommand_buildsCommandFromRegistry() {
        CreateAssetCommand result = registry.createCreateCommand(createRequest);

        assertThat(result.id()).isEqualTo("asset-1");
        assertThat(result.type()).isEqualTo(AssetType.CRE);
        assertThat(result.status()).isEqualTo("ACTIVE");
        assertThat(result.subtype()).isEqualTo("OFFICE");
        assertThat(result.notionalAmount()).isEqualTo(new BigDecimal("150.00"));
        assertThat(result.year()).isEqualTo(2024);
        assertThat(result.description()).isEqualTo("Created");
        assertThat(result.currency()).isEqualTo("USD");
        assertThat(result.attributes()).containsExactly(
                new AVString("city", "Frankfurt"),
                new AVDecimal("area", new BigDecimal("500.25")),
                new AVBoolean("active", true)
        );
    }

    @Test
    void createPatchCommand_withExplicitId_parsesAttributesFromSchema() {
        PatchAssetCommand result = registry.createPatchCommand(AssetType.SHIP, "asset-2", patchRequest);

        assertThat(result.assetId()).isEqualTo("asset-2");
        assertThat(result.status()).isEqualTo("INACTIVE");
        assertThat(result.subtype()).isEqualTo("FREIGHT");
        assertThat(result.description()).isEqualTo("Patched");
        assertThat(result.currency()).isEqualTo("EUR");
        assertThat(result.attributes()).containsExactly(
                new AVString("name", "Sea Queen"),
                new AVDecimal("imo", new BigDecimal("9876543")),
                new AVBoolean("active", false)
        );
    }

    @Test
    void createPatchCommand_usingRequestId_validatesPresence() {
        patchRequest.setId("asset-3");

        PatchAssetCommand result = registry.createPatchCommand(AssetType.SHIP, patchRequest);

        assertThat(result.assetId()).isEqualTo("asset-3");
        assertThat(result.attributes()).containsExactly(
                new AVString("name", "Sea Queen"),
                new AVDecimal("imo", new BigDecimal("9876543")),
                new AVBoolean("active", false)
        );
    }

    @Test
    void createPatchCommand_withoutRequestId_throwsException() {
        assertThatThrownBy(() -> registry.createPatchCommand(AssetType.CRE, patchRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("asset id");
    }

    private AttributeJsonReader createJsonReader() {
        TypeSchemaRegistry typeSchemaRegistry = new TypeSchemaRegistry(objectMapper);
        typeSchemaRegistry.discover();

        AttributeDefinitionRegistry attributeDefinitionRegistry = new AttributeDefinitionRegistry(objectMapper, typeSchemaRegistry);
        attributeDefinitionRegistry.rebuild();

        return new AttributeJsonReader(objectMapper, attributeDefinitionRegistry);
    }
}
