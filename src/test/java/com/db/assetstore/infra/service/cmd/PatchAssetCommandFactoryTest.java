package com.db.assetstore.infra.service.cmd;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;
import com.db.assetstore.domain.service.cmd.factory.PatchAssetCommandFactory;
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

    private AssetPatchRequest request;

    @BeforeEach
    void setUp() {
        AttributeJsonReader attributeJsonReader = createJsonReader();
        factory = new PatchAssetCommandFactory(attributeJsonReader);

        ObjectNode attributes = objectMapper.createObjectNode();
        attributes.put("name", "Sea Queen");
        attributes.put("imo", 9876543);
        attributes.put("active", false);

        request = new AssetPatchRequest();
        request.setId("asset-2");
        request.setStatus("INACTIVE");
        request.setSubtype("FREIGHT");
        request.setNotionalAmount(new BigDecimal("321.10"));
        request.setYear(2025);
        request.setDescription("Patched");
        request.setCurrency("EUR");
        request.setAttributes(attributes);
        request.setExecutedBy("patcher");
    }

    @Test
    void createCommand_buildsCommandWithAttributes() {
        PatchAssetCommand command = factory.createCommand(AssetType.SHIP, "asset-2", request);

        assertThat(command.assetId()).isEqualTo("asset-2");
        assertThat(command.status()).isEqualTo("INACTIVE");
        assertThat(command.subtype()).isEqualTo("FREIGHT");
        assertThat(command.notionalAmount()).isEqualTo(new BigDecimal("321.10"));
        assertThat(command.year()).isEqualTo(2025);
        assertThat(command.description()).isEqualTo("Patched");
        assertThat(command.currency()).isEqualTo("EUR");
        assertThat(command.attributes()).containsExactly(
                new AVString("name", "Sea Queen"),
                new AVDecimal("imo", new BigDecimal("9876543")),
                new AVBoolean("active", false)
        );
        assertThat(command.executedBy()).isEqualTo("patcher");
        assertThat(command.requestTime()).isNotNull();
    }

    @Test
    void createCommand_withNullAttributes_usesEmptyList() {
        request.setAttributes(null);

        PatchAssetCommand command = factory.createCommand(AssetType.CRE, "asset-2", request);

        assertThat(command.attributes()).isEmpty();
        assertThat(command.executedBy()).isEqualTo("patcher");
        assertThat(command.requestTime()).isNotNull();
    }

    @Test
    void createCommand_withoutExecutor_throwsException() {
        AssetPatchRequest withoutExecutor = new AssetPatchRequest();
        withoutExecutor.setId("asset-3");

        assertThatThrownBy(() -> factory.createCommand(AssetType.CRE, "asset-3", withoutExecutor))
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
