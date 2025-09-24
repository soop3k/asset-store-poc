package com.db.assetstore.infra.service.cmd;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
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

class AssetCommandFactoryRegistryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AssetCommandFactoryRegistry registry;

    @BeforeEach
    void setUp() {
        AttributeJsonReader attributeJsonReader = createJsonReader();
        registry = new AssetCommandFactoryRegistry(
                new CreateAssetCommandFactory(attributeJsonReader),
                new PatchAssetCommandFactory(attributeJsonReader),
                new DeleteAssetCommandFactory()
        );
    }

    @Test
    void createCreateCommand_parsesAttributes() {
        ObjectNode attributes = objectMapper.createObjectNode();
        attributes.put("city", "Paris");
        attributes.put("area", new BigDecimal("275.75"));
        attributes.put("active", true);

        AssetCreateRequest request = new AssetCreateRequest(
                "asset-7",
                AssetType.CRE,
                "ACTIVE",
                "OFFICE",
                new BigDecimal("300.00"),
                2022,
                "Command registry test",
                "USD",
                attributes,
                "creator"
        );

        var command = registry.createCreateCommand(request);

        assertThat(command.id()).isEqualTo("asset-7");
        assertThat(command.attributes()).containsExactly(
                new AVString("city", "Paris"),
                new AVDecimal("area", new BigDecimal("275.75")),
                new AVBoolean("active", true)
        );
        assertThat(command.executedBy()).isEqualTo("creator");
        assertThat(command.requestTime()).isNotNull();
    }

    @Test
    void createPatchCommand_withExplicitId_parsesAttributes() {
        AssetPatchRequest request = new AssetPatchRequest();
        request.setStatus("INACTIVE");
        request.setAttributes(objectMapper.createObjectNode()
                .put("name", "Aurora")
                .put("imo", 13579)
                .put("active", false));
        request.setExecutedBy("updater");

        var command = registry.createPatchCommand(AssetType.SHIP, "ship-11", request);

        assertThat(command.assetId()).isEqualTo("ship-11");
        assertThat(command.attributes()).containsExactly(
                new AVString("name", "Aurora"),
                new AVDecimal("imo", new BigDecimal("13579")),
                new AVBoolean("active", false)
        );
        assertThat(command.executedBy()).isEqualTo("updater");
        assertThat(command.requestTime()).isNotNull();
    }

    @Test
    void createPatchCommand_withIdInRequest_usesRequestValue() {
        AssetPatchRequest request = new AssetPatchRequest();
        request.setId("ship-22");
        request.setAttributes(objectMapper.createObjectNode()
                .put("name", "Baltic Star"));
        request.setExecutedBy("patcher");

        var command = registry.createPatchCommand(AssetType.SHIP, request);

        assertThat(command.assetId()).isEqualTo("ship-22");
        assertThat(command.attributes()).containsExactly(
                new AVString("name", "Baltic Star")
        );
        assertThat(command.executedBy()).isEqualTo("patcher");
        assertThat(command.requestTime()).isNotNull();
    }

    @Test
    void createPatchCommand_withoutId_throwsException() {
        AssetPatchRequest request = new AssetPatchRequest();
        request.setAttributes(objectMapper.createObjectNode());
        request.setExecutedBy("tester");

        assertThatThrownBy(() -> registry.createPatchCommand(AssetType.CRE, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("asset id");
    }

    @Test
    void createDeleteCommand_validatesIds() {
        AssetDeleteRequest request = new AssetDeleteRequest("asset-33", "tester");

        var command = registry.createDeleteCommand("asset-33", request);

        assertThat(command.assetId()).isEqualTo("asset-33");
        assertThat(command.executedBy()).isEqualTo("tester");

        assertThatThrownBy(() -> registry.createDeleteCommand("asset-33", new AssetDeleteRequest("other", "tester")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("match");
    }

    private AttributeJsonReader createJsonReader() {
        TypeSchemaRegistry typeSchemaRegistry = new TypeSchemaRegistry();
        typeSchemaRegistry.discover();

        AttributeDefinitionRegistry attributeDefinitionRegistry =
                new AttributeDefinitionRegistry(objectMapper, typeSchemaRegistry);
        attributeDefinitionRegistry.rebuild();

        return new AttributeJsonReader(objectMapper, attributeDefinitionRegistry);
    }
}
