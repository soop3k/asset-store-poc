package com.db.assetstore.infra.service.cmd;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.db.assetstore.domain.schema.TypeSchemaRegistry;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateAssetCommandFactoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private CreateAssetCommandFactory factory;

    @BeforeEach
    void setUp() {
        AttributeJsonReader attributeJsonReader = createJsonReader();
        factory = new CreateAssetCommandFactory(attributeJsonReader);
    }

    @Test
    void create_buildsCommandWithParsedAttributes() {
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

        var command = factory.create(request);

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
    }

    @Test
    void create_withoutExecutor_throwsException() {
        AssetCreateRequest request = new AssetCreateRequest(
                "asset-2",
                AssetType.CRE,
                null,
                null,
                null,
                null,
                null,
                null,
                objectMapper.createObjectNode(),
                ""
        );

        assertThatThrownBy(() -> factory.create(request))
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
