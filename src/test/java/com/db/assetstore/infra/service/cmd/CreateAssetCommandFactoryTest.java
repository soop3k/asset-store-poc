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

    @BeforeEach
    void setUp() {
        AttributeJsonReader attributeJsonReader = createJsonReader();
        factory = new CreateAssetCommandFactory(attributeJsonReader);
    }

    @Test
    void createCommand_populatesCommandAndParsesAttributes() {
        ObjectNode attributes = objectMapper.createObjectNode();
        attributes.put("city", "Frankfurt");
        attributes.put("area", new BigDecimal("500.25"));
        attributes.put("active", true);

        AssetCreateRequest request = new AssetCreateRequest(
                "asset-1",
                AssetType.CRE,
                "ACTIVE",
                "OFFICE",
                new BigDecimal("150.00"),
                2024,
                "Created",
                "USD",
                attributes,
                "creator"
        );

        CreateAssetCommand command = factory.createCommand(request);

        assertThat(command.id()).isEqualTo("asset-1");
        assertThat(command.type()).isEqualTo(AssetType.CRE);
        assertThat(command.status()).isEqualTo("ACTIVE");
        assertThat(command.subtype()).isEqualTo("OFFICE");
        assertThat(command.notionalAmount()).isEqualTo(new BigDecimal("150.00"));
        assertThat(command.year()).isEqualTo(2024);
        assertThat(command.description()).isEqualTo("Created");
        assertThat(command.currency()).isEqualTo("USD");
        assertThat(command.attributes()).containsExactly(
                new AVString("city", "Frankfurt"),
                new AVDecimal("area", new BigDecimal("500.25")),
                new AVBoolean("active", true)
        );
        assertThat(command.executedBy()).isEqualTo("creator");
        assertThat(command.requestTime()).isNotNull();
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
                null,
                "executor"
        );

        CreateAssetCommand command = factory.createCommand(request);

        assertThat(command.attributes()).isEmpty();
        assertThat(command.executedBy()).isEqualTo("executor");
        assertThat(command.requestTime()).isNotNull();
    }

    private AttributeJsonReader createJsonReader() {
        TypeSchemaRegistry typeSchemaRegistry = new TypeSchemaRegistry(objectMapper);
        typeSchemaRegistry.discover();

        AttributeDefinitionRegistry attributeDefinitionRegistry =
                new AttributeDefinitionRegistry(objectMapper, typeSchemaRegistry);
        attributeDefinitionRegistry.rebuild();

        return new AttributeJsonReader(objectMapper, attributeDefinitionRegistry);
    }
}
