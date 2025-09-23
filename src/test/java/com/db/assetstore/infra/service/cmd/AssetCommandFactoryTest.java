package com.db.assetstore.infra.service.cmd;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.cmd.DeleteAssetCommand;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.db.assetstore.domain.schema.TypeSchemaRegistry;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
import com.db.assetstore.infra.api.dto.AssetDeleteRequest;
import com.db.assetstore.infra.api.dto.AssetPatchRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssetCommandFactoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AssetCommandFactory factory;

    private AssetCreateRequest createRequest;

    private AssetPatchRequest patchRequest;

    private ObjectNode createAttributes;

    private ObjectNode patchAttributes;

    @BeforeEach
    void setUp() {
        AttributeJsonReader attributeJsonReader = createJsonReader();
        factory = new AssetCommandFactory(attributeJsonReader);

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
    void createCreateCommand_buildsCommand() {
        CreateAssetCommand result = factory.createCreateCommand(createRequest);

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
        PatchAssetCommand result = factory.createPatchCommand(AssetType.SHIP, "asset-2", patchRequest);

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

        PatchAssetCommand result = factory.createPatchCommand(AssetType.SHIP, patchRequest);

        assertThat(result.assetId()).isEqualTo("asset-3");
        assertThat(result.attributes()).containsExactly(
                new AVString("name", "Sea Queen"),
                new AVDecimal("imo", new BigDecimal("9876543")),
                new AVBoolean("active", false)
        );
    }

    @Test
    void createPatchCommand_withoutRequestId_throwsException() {
        assertThatThrownBy(() -> factory.createPatchCommand(AssetType.CRE, patchRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("asset id");
    }

    @Test
    void createPatchCommand_withBlankExplicitId_throwsException() {
        assertThatThrownBy(() -> factory.createPatchCommand(AssetType.CRE, " ", patchRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("assetId must not be blank");
    }

    @Test
    void createDeleteCommand_validatesAndBuildsCommand() {
        DeleteAssetCommand result = factory.createDeleteCommand("asset-5", new AssetDeleteRequest("asset-5", "deleter"));

        assertThat(result.assetId()).isEqualTo("asset-5");
        assertThat(result.deletedBy()).isEqualTo("deleter");
        assertThat(result.requestTime()).isNotNull();
    }

    @Test
    void createDeleteCommand_withMismatchedId_throwsException() {
        assertThatThrownBy(() -> factory.createDeleteCommand("asset-6", new AssetDeleteRequest("other", "deleter")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("match");
    }

    @Test
    void createDeleteCommand_withBlankId_throwsException() {
        assertThatThrownBy(() -> factory.createDeleteCommand(" ", new AssetDeleteRequest("asset-9", "user")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("assetId must not be blank");
    }

    private AttributeJsonReader createJsonReader() {
        TypeSchemaRegistry typeSchemaRegistry = new TypeSchemaRegistry();
        typeSchemaRegistry.discover();

        AttributeDefinitionRegistry attributeDefinitionRegistry = new AttributeDefinitionRegistry(objectMapper, typeSchemaRegistry);
        attributeDefinitionRegistry.rebuild();

        return new AttributeJsonReader(objectMapper, attributeDefinitionRegistry);
    }
}
