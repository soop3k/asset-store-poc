package com.db.assetstore.infra.service.cmd;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.db.assetstore.domain.schema.TypeSchemaRegistry;
import com.db.assetstore.infra.api.dto.AssetPatchRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PatchAssetCommandFactoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private PatchAssetCommandFactory factory;

    private AssetPatchRequest request;
    private ObjectNode attributes;

    @BeforeEach
    void setUp() {
        AttributeJsonReader attributeJsonReader = createJsonReader();
        factory = new PatchAssetCommandFactory(attributeJsonReader);

        attributes = objectMapper.createObjectNode();
        attributes.put("rooms", 5);
        attributes.put("active", false);

        request = new AssetPatchRequest();
        request.setId("asset-2");
        request.setStatus("ACTIVE");
        request.setSubtype("OFFICE");
        request.setNotionalAmount(new BigDecimal("321.10"));
        request.setYear(2025);
        request.setDescription("Updated");
        request.setCurrency("EUR");
        request.setAttributes(attributes);
    }

    @Test
    void createCommand_buildsCommandWithAttributes() {
        PatchAssetCommand cmd = factory.createCommand(AssetType.CRE, "asset-2", request);

        assertThat(cmd.assetId()).isEqualTo("asset-2");
        assertThat(cmd.status()).isEqualTo("ACTIVE");
        assertThat(cmd.subtype()).isEqualTo("OFFICE");
        assertThat(cmd.notionalAmount()).isEqualTo(new BigDecimal("321.10"));
        assertThat(cmd.year()).isEqualTo(2025);
        assertThat(cmd.description()).isEqualTo("Updated");
        assertThat(cmd.currency()).isEqualTo("EUR");
        assertThat(cmd.attributes()).containsExactly(
                new AVDecimal("rooms", new BigDecimal("5")),
                new AVBoolean("active", false)
        );
    }

    @Test
    void createCommand_withNullAttributes_usesEmptyList() {
        request.setAttributes(null);

        PatchAssetCommand cmd = factory.createCommand(AssetType.SHIP, "asset-2", request);

        assertThat(cmd.attributes()).isEmpty();
    }

    private AttributeJsonReader createJsonReader() {
        TypeSchemaRegistry typeSchemaRegistry = new TypeSchemaRegistry();
        typeSchemaRegistry.discover();

        AttributeDefinitionRegistry attributeDefinitionRegistry = new AttributeDefinitionRegistry(objectMapper, typeSchemaRegistry);
        attributeDefinitionRegistry.rebuild();

        return new AttributeJsonReader(objectMapper, attributeDefinitionRegistry);
    }
}
