package com.db.assetstore.infra.service.cmd;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.db.assetstore.domain.schema.TypeSchemaRegistry;
import com.db.assetstore.infra.api.dto.AssetPatchRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PatchAssetCommandFactoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private PatchAssetCommandFactory factory;

    @BeforeEach
    void setUp() {
        AttributeJsonReader attributeJsonReader = createJsonReader();
        factory = new PatchAssetCommandFactory(attributeJsonReader);
    }

    @Test
    void create_buildsPatchCommandUsingContext() {
        ObjectNode attributes = objectMapper.createObjectNode();
        attributes.put("name", "Sea Queen");
        attributes.put("imo", 9876543);
        attributes.put("active", false);

        AssetPatchRequest request = new AssetPatchRequest();
        request.setStatus("INACTIVE");
        request.setSubtype("FREIGHT");
        request.setDescription("Patched");
        request.setCurrency("EUR");
        request.setAttributes(attributes);
        request.setExecutedBy("updater");

        var command = factory.create(AssetType.SHIP, "asset-2", request);

        assertThat(command.assetId()).isEqualTo("asset-2");
        assertThat(command.status()).isEqualTo("INACTIVE");
        assertThat(command.subtype()).isEqualTo("FREIGHT");
        assertThat(command.description()).isEqualTo("Patched");
        assertThat(command.currency()).isEqualTo("EUR");
        assertThat(command.attributes()).containsExactly(
                new AVString("name", "Sea Queen"),
                new AVDecimal("imo", new BigDecimal("9876543")),
                new AVBoolean("active", false)
        );
        assertThat(command.executedBy()).isEqualTo("updater");
    }

    @Test
    void create_withoutExecutor_throwsException() {
        AssetPatchRequest request = new AssetPatchRequest();
        request.setId("asset-3");

        assertThatThrownBy(() -> factory.create(AssetType.CRE, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("executedBy");
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
