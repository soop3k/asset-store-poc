package com.db.assetstore.infra.service.cmd;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.model.type.AttributeType;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;
import com.db.assetstore.domain.service.cmd.factory.PatchAssetCommandFactory;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.db.assetstore.domain.service.validation.AttributeValidator;
import com.db.assetstore.domain.service.validation.rule.AttributeValidationErrorsException;
import com.db.assetstore.domain.service.validation.rule.CustomValidationRuleRegistry;
import com.db.assetstore.domain.service.validation.rule.ValidationRuleFactory;
import com.db.assetstore.infra.api.dto.AssetPatchRequest;
import com.db.assetstore.infra.json.AttributeJsonReader;
import com.db.assetstore.infra.json.AttributePayloadParser;
import com.db.assetstore.infra.json.AttributeValueAssembler;
import com.db.assetstore.testutil.InMemoryAttributeDefinitionLoader;
import com.db.assetstore.testutil.validation.MatchingAttributesRule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static com.db.assetstore.testutil.AttributeTestHelpers.constraint;
import static com.db.assetstore.testutil.AttributeTestHelpers.definition;
import static com.db.assetstore.domain.service.type.ConstraintDefinition.Rule.REQUIRED;
import static com.db.assetstore.domain.service.type.ConstraintDefinition.Rule.TYPE;

class PatchAssetCommandFactoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private PatchAssetCommandFactory factory;
    private AssetPatchRequest request;
    private AttributeDefinitionRegistry registry;
    private CustomValidationRuleRegistry customRegistry;

    @BeforeEach
    void setUp() {
        var name = definition(AssetType.SHIP, "name", AttributeType.STRING);
        var imo = definition(AssetType.SHIP, "imo", AttributeType.DECIMAL);
        var active = definition(AssetType.SHIP, "active", AttributeType.BOOLEAN);

        registry = InMemoryAttributeDefinitionLoader.builder()
                .withAttribute(name, constraint(name, TYPE), constraint(name, REQUIRED))
                .withAttribute(imo, constraint(imo, TYPE))
                .withAttribute(active, constraint(active, TYPE))
                .withAssetType(AssetType.CRE)
                .buildRegistry();
        customRegistry = new CustomValidationRuleRegistry(List.of(new MatchingAttributesRule()));
        AttributeValidator validator = new AttributeValidator(registry, ruleFactory(customRegistry));
        AttributeJsonReader reader = new AttributeJsonReader(
                new AttributePayloadParser(),
                new AttributeValueAssembler(registry)
        );
        factory = new PatchAssetCommandFactory(validator, reader);

        request = new AssetPatchRequest();
        request.setId("asset-2");
        request.setStatus("INACTIVE");
        request.setSubtype("FREIGHT");
        request.setNotionalAmount(new BigDecimal("321.10"));
        request.setYear(2025);
        request.setDescription("Patched");
        request.setCurrency("EUR");
        request.setAttributes(attributesNode("Sea Queen", new BigDecimal("9876543"), false));
        request.setExecutedBy("patcher");
    }

    @Test
    void buildsCommandWithAttributes() {
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
    void buildCommandWithNullAttributes() {
        request.setAttributes(null);

        PatchAssetCommand command = factory.createCommand(AssetType.CRE, "asset-2", request);

        assertThat(command.attributes()).isEmpty();
        assertThat(command.executedBy()).isEqualTo("patcher");
        assertThat(command.requestTime()).isNotNull();
    }

    @Test
    void rejectsNullForRequiredAttributeOnPartialUpdate() {
        ObjectNode node = objectMapper.createObjectNode();
        node.putNull("name");
        request.setAttributes(node);

        assertThatThrownBy(() -> factory.createCommand(AssetType.SHIP, "asset-2", request))
                .isInstanceOf(AttributeValidationErrorsException.class)
                .satisfies(ex -> {
                    var errors = (AttributeValidationErrorsException) ex;
                    assertThat(errors.violations()).anySatisfy(v -> {
                        assertThat(v.rule()).isEqualTo(REQUIRED.name());
                        assertThat(v.actual()).isNull();
                    });
                });
    }

    private ValidationRuleFactory ruleFactory(CustomValidationRuleRegistry customRegistry) {
        return new ValidationRuleFactory(customRegistry);
    }

    private ObjectNode attributesNode(String name, BigDecimal imo, boolean active) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("name", name);
        node.put("imo", imo);
        node.put("active", active);
        return node;
    }

}
