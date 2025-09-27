package com.db.assetstore.infra.json;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.model.type.AttributeType;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.db.assetstore.infra.config.JsonMapperProvider;
import com.db.assetstore.testutil.InMemoryAttributeDefinitionLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.db.assetstore.domain.service.type.ConstraintDefinition.Rule.TYPE;
import static com.db.assetstore.testutil.AttributeTestHelpers.constraint;
import static com.db.assetstore.testutil.AttributeTestHelpers.definition;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttributeJsonReaderTest {

    private final ObjectMapper objectMapper = new JsonMapperProvider().objectMapper();
    private AttributeJsonReader reader;

    @BeforeEach
    void setUp() {
        var city = definition(AssetType.CRE, "city", AttributeType.STRING);
        var area = definition(AssetType.CRE, "area", AttributeType.DECIMAL);
        var active = definition(AssetType.CRE, "active", AttributeType.BOOLEAN);

        AttributeDefinitionRegistry registry = InMemoryAttributeDefinitionLoader.builder()
                .withAttribute(city, constraint(city, TYPE))
                .withAttribute(area, constraint(area, TYPE))
                .withAttribute(active, constraint(active, TYPE))
                .buildRegistry();

        reader = new AttributeJsonReader(
                new AttributePayloadParser(),
                new AttributeValueAssembler(registry)
        );
    }

    @Test
    void parsesValuesUsingDefinitions() {
        var payload = objectMapper.createObjectNode();
        payload.put("city", "Warsaw");
        payload.put("active", true);
        payload.putArray("area").add("123.45").add("678.90");

        var result = reader.read(AssetType.CRE, payload);

        assertThat(result).extracting(
                AttributesCollection::asList
        ).isEqualTo(
                AttributesCollection.fromFlat(List.of(
                        new AVString("city", "Warsaw"),
                        new AVBoolean("active", true),
                        new AVDecimal("area", new BigDecimal("123.45")),
                        new AVDecimal("area", new BigDecimal("678.90"))
                )).asList()
        );
    }

    @Test
    void failsWhenDefinitionMissing() {
        var payload = objectMapper.createObjectNode();
        payload.put("unknown", "value");

        assertThatThrownBy(() -> reader.read(AssetType.CRE, payload))
                .isInstanceOf(AttributeParsingException.class)
                .hasMessageContaining("Unknown attribute definition");
    }

    @Test
    void failsWhenBooleanValueInvalid() {
        var payload = objectMapper.createObjectNode();
        payload.put("active", "not-bool");

        assertThatThrownBy(() -> reader.read(AssetType.CRE, payload))
                .isInstanceOf(AttributeParsingException.class)
                .hasMessageContaining("expects type BOOLEAN");
    }

    @Test
    void failsWhenStringValueIsObject() {
        var payload = objectMapper.createObjectNode();
        var nested = objectMapper.createObjectNode();
        nested.put("city", "Warsaw");
        payload.set("city", nested);

        assertThatThrownBy(() -> reader.read(AssetType.CRE, payload))
                .isInstanceOf(AttributeParsingException.class)
                .hasMessageContaining("expects type STRING");
    }

}
