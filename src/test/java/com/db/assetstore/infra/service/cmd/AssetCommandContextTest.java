package com.db.assetstore.infra.service.cmd;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.db.assetstore.domain.schema.TypeSchemaRegistry;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
import com.db.assetstore.infra.api.dto.AssetPatchRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssetCommandContextTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private AttributeJsonReader jsonReader;

    @BeforeEach
    void setUp() {
        jsonReader = createJsonReader();
    }

    @Test
    void forCreate_parsesAttributesUsingSchemaDefinitions() {
        ObjectNode attributes = objectMapper.createObjectNode();
        attributes.put("city", "Warsaw");
        attributes.put("area", new BigDecimal("123.45"));
        attributes.put("active", true);

        AssetCreateRequest request = new AssetCreateRequest(
                "asset-1",
                AssetType.CRE,
                "ACTIVE",
                "OFFICE",
                new BigDecimal("100.00"),
                2023,
                "Created",
                "EUR",
                attributes
        );

        AssetCommandContext context = AssetCommandContext.forCreate(jsonReader, request);

        assertThat(context.assetType()).isEqualTo(AssetType.CRE);
        assertThat(context.assetId()).isEqualTo("asset-1");
        assertThat(context.attributes()).containsExactly(
                new AVString("city", "Warsaw"),
                new AVDecimal("area", new BigDecimal("123.45")),
                new AVBoolean("active", true)
        );
    }

    @Test
    void forPatch_withExplicitId_parsesAttributes() {
        AssetPatchRequest request = new AssetPatchRequest();
        request.setAttributes(objectMapper.createObjectNode()
                .put("name", "Aurora")
                .put("imo", 1234567)
                .put("active", false));

        AssetCommandContext context = AssetCommandContext.forPatch(jsonReader, AssetType.SHIP, "asset-22", request);

        assertThat(context.assetId()).isEqualTo("asset-22");
        assertThat(context.attributes()).containsExactly(
                new AVString("name", "Aurora"),
                new AVDecimal("imo", new BigDecimal("1234567")),
                new AVBoolean("active", false)
        );
    }

    @Test
    void forPatch_withoutIdInRequest_throwsException() {
        AssetPatchRequest request = new AssetPatchRequest();
        request.setAttributes(objectMapper.createObjectNode());

        assertThatThrownBy(() -> AssetCommandContext.forPatch(jsonReader, AssetType.CRE, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("asset id");
    }

    @Test
    void forPatch_withBlankExplicitId_throwsException() {
        AssetPatchRequest request = new AssetPatchRequest();
        request.setAttributes(objectMapper.createObjectNode());

        assertThatThrownBy(() -> AssetCommandContext.forPatch(jsonReader, AssetType.SHIP, " ", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("assetId must not be blank");
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
