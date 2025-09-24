package com.db.assetstore.infra.service.cmd;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.cmd.factory.CreateAssetCommandFactory;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.db.assetstore.domain.service.type.TypeSchemaRegistry;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CreateAssetCommandFactoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private CreateAssetCommandFactory factory;

    private ObjectNode attributes;

    @BeforeEach
    void setUp() {
        AttributeJsonReader attributeJsonReader = createJsonReader();
        factory = new CreateAssetCommandFactory(attributeJsonReader);

        attributes = objectMapper.createObjectNode();
        attributes.put("city", "Berlin");
        attributes.put("area", new BigDecimal("321.75"));
        attributes.put("active", true);
    }

    @Test
    void createCommand_populatesCommandAndParsesAttributes() {
        AssetCreateRequest request = new AssetCreateRequest(
                "asset-1",
                AssetType.CRE,
                "ACTIVE",
                "OFFICE",
                new BigDecimal("123.45"),
                2024,
                "Desc",
                "USD",
                attributes
        );

        CreateAssetCommand cmd = factory.createCommand(request);

        assertThat(cmd.id()).isEqualTo("asset-1");
        assertThat(cmd.type()).isEqualTo(AssetType.CRE);
        assertThat(cmd.status()).isEqualTo("ACTIVE");
        assertThat(cmd.subtype()).isEqualTo("OFFICE");
        assertThat(cmd.notionalAmount()).isEqualTo(new BigDecimal("123.45"));
        assertThat(cmd.year()).isEqualTo(2024);
        assertThat(cmd.description()).isEqualTo("Desc");
        assertThat(cmd.currency()).isEqualTo("USD");
        assertThat(cmd.attributes()).containsExactly(
                new AVString("city", "Berlin"),
                new AVDecimal("area", new BigDecimal("321.75")),
                new AVBoolean("active", true)
        );
    }

    @Test
    void createCommand_withNullAttributes_usesEmptyList() {
        AssetCreateRequest request = new AssetCreateRequest(
                null,
                AssetType.SHIP,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        CreateAssetCommand cmd = factory.createCommand(request);

        assertThat(cmd.attributes()).isEmpty();
    }

    private AttributeJsonReader createJsonReader() {
        TypeSchemaRegistry typeSchemaRegistry = new TypeSchemaRegistry(objectMapper);
        typeSchemaRegistry.discover();

        AttributeDefinitionRegistry attributeDefinitionRegistry = new AttributeDefinitionRegistry(objectMapper, typeSchemaRegistry);
        attributeDefinitionRegistry.rebuild();

        return new AttributeJsonReader(objectMapper, attributeDefinitionRegistry);
    }
}
