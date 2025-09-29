package com.db.assetstore.infra.service.cmd;

import com.db.assetstore.domain.model.asset.AssetType;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.model.type.AttributeType;
import com.db.assetstore.domain.service.asset.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.asset.cmd.factory.CreateAssetCommandFactory;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.db.assetstore.domain.service.validation.AttributeValidator;
import com.db.assetstore.domain.service.validation.rule.CustomValidationRuleRegistry;
import com.db.assetstore.domain.service.validation.rule.ValidationRuleFactory;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
import com.db.assetstore.infra.json.reader.AttributeJsonReader;
import com.db.assetstore.infra.json.reader.AttributePayloadParser;
import com.db.assetstore.infra.json.reader.AttributeValueAssembler;
import com.db.assetstore.testutil.validation.MatchingAttributesRule;
import com.db.assetstore.testutil.InMemoryAttributeDefinitionLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static com.db.assetstore.testutil.AttributeTestHelpers.constraint;
import static com.db.assetstore.testutil.AttributeTestHelpers.definition;
import static com.db.assetstore.domain.service.type.ConstraintDefinition.Rule.TYPE;

class CreateAssetCommandFactoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private CreateAssetCommandFactory factory;
    private AttributeDefinitionRegistry registry;
    private CustomValidationRuleRegistry customRegistry;

    @BeforeEach
    void setUp() {
        var city = definition(AssetType.CRE, "city", AttributeType.STRING);
        var area = definition(AssetType.CRE, "area", AttributeType.DECIMAL);
        var active = definition(AssetType.CRE, "active", AttributeType.BOOLEAN);

        registry = InMemoryAttributeDefinitionLoader.builder()
                .withAttribute(city, constraint(city, TYPE))
                .withAttribute(area, constraint(area, TYPE))
                .withAttribute(active, constraint(active, TYPE))
                .withAssetType(AssetType.SHIP)
                .buildRegistry();
        customRegistry = new CustomValidationRuleRegistry(List.of(new MatchingAttributesRule()));
        var validator = new AttributeValidator(registry, ruleFactory(customRegistry));
        AttributeJsonReader reader = new AttributeJsonReader(
                new AttributePayloadParser(),
                new AttributeValueAssembler(registry)
        );
        factory = new CreateAssetCommandFactory(validator, reader);
    }

    @Test
    void populatesCommandAndParsesAttributes() {
        AssetCreateRequest request = new AssetCreateRequest(
                "asset-1",
                AssetType.CRE,
                "ACTIVE",
                "OFFICE",
                new BigDecimal("150.00"),
                2024,
                "Created",
                "USD",
                attributesNode("Frankfurt", new BigDecimal("500.25"), true),
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
    void buildCommandWithNullAttributes() {
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

    private ValidationRuleFactory ruleFactory(CustomValidationRuleRegistry customRegistry) {
        return new ValidationRuleFactory(customRegistry);
    }

    private ObjectNode attributesNode(String city, BigDecimal area, boolean active) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("city", city);
        node.put("area", area);
        node.put("active", active);
        return node;
    }

}
