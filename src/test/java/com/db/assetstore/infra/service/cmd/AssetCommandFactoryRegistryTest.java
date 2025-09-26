package com.db.assetstore.infra.service.cmd;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.model.type.AttributeType;
import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.cmd.DeleteAssetCommand;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;
import com.db.assetstore.domain.service.cmd.factory.AssetCommandFactoryRegistry;
import com.db.assetstore.domain.service.cmd.factory.CreateAssetCommandFactory;
import com.db.assetstore.domain.service.cmd.factory.DeleteAssetCommandFactory;
import com.db.assetstore.domain.service.cmd.factory.PatchAssetCommandFactory;
import com.db.assetstore.domain.service.type.AttributeDefinition;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.db.assetstore.domain.service.type.ConstraintDefinition;
import com.db.assetstore.domain.service.validation.AttributeValidator;
import com.db.assetstore.domain.service.validation.custom.MatchingAttributesRule;
import com.db.assetstore.domain.service.validation.rule.CustomValidationRuleRegistry;
import com.db.assetstore.domain.service.validation.rule.ValidationRuleFactory;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
import com.db.assetstore.infra.api.dto.AssetDeleteRequest;
import com.db.assetstore.infra.api.dto.AssetPatchRequest;
import com.db.assetstore.infra.json.AttributeJsonReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssetCommandFactoryRegistryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private AssetCommandFactoryRegistry registry;
    private AttributeDefinitionRegistry attributeDefinitionRegistry;
    private ValidationRuleFactory validationRuleFactory;
    private CustomValidationRuleRegistry customRegistry;
    private AssetCreateRequest createRequest;
    private AssetPatchRequest patchRequest;

    @BeforeEach
    void setUp() {

        attributeDefinitionRegistry = new FixedRegistry();
        customRegistry = new CustomValidationRuleRegistry(List.of(new MatchingAttributesRule()));
        validationRuleFactory = ruleFactory(customRegistry);
        AttributeValidator attributeValidator = new AttributeValidator(attributeDefinitionRegistry, validationRuleFactory);
        AttributeJsonReader reader = new AttributeJsonReader(objectMapper, attributeDefinitionRegistry);

        registry = new AssetCommandFactoryRegistry(
                new CreateAssetCommandFactory(attributeValidator, reader),
                new PatchAssetCommandFactory(attributeValidator, reader),
                new DeleteAssetCommandFactory()
        );

        createRequest = new AssetCreateRequest(
                "asset-1",
                AssetType.CRE,
                "ACTIVE",
                "OFFICE",
                new BigDecimal("150.00"),
                2024,
                "Created",
                "USD",
                createAttributesNode(),
                "creator"
        );

        patchRequest = new AssetPatchRequest();
        patchRequest.setStatus("INACTIVE");
        patchRequest.setSubtype("FREIGHT");
        patchRequest.setDescription("Patched");
        patchRequest.setCurrency("EUR");
        patchRequest.setAttributes(patchAttributesNode());
        patchRequest.setExecutedBy("patcher");
    }

    @Test
    void buildsCommandFromRegistry() {
        CreateAssetCommand result = registry.createCreateCommand(createRequest);

        assertThat(result.id()).isEqualTo("asset-1");
        assertThat(result.type()).isEqualTo(AssetType.CRE);
        assertThat(result.status()).isEqualTo("ACTIVE");
        assertThat(result.subtype()).isEqualTo("OFFICE");
        assertThat(result.notionalAmount()).isEqualTo(new BigDecimal("150.00"));
        assertThat(result.year()).isEqualTo(2024);
        assertThat(result.description()).isEqualTo("Created");
        assertThat(result.currency()).isEqualTo("USD");
        assertThat(result.attributes()).containsExactly(
                new AVString("city", "Frankfurt"),
                new AVDecimal("area", new BigDecimal("500.25")),
                new AVBoolean("active", true)
        );
        assertThat(result.executedBy()).isEqualTo("creator");
        assertThat(result.requestTime()).isNotNull();
    }

    @Test
    void buildCommandWithExplicitId() {
        PatchAssetCommand result = registry.createPatchCommand(AssetType.SHIP, "asset-2", patchRequest);

        assertThat(result.assetId()).isEqualTo("asset-2");
        assertThat(result.status()).isEqualTo("INACTIVE");
        assertThat(result.subtype()).isEqualTo("FREIGHT");
        assertThat(result.description()).isEqualTo("Patched");
        assertThat(result.currency()).isEqualTo("EUR");
        assertThat(result.attributes()).containsExactly(
                new AVString("name", "Sea Queen"),
                new AVDecimal("imo", new BigDecimal("9876543")),
                new AVBoolean("active", false)
        );
        assertThat(result.executedBy()).isEqualTo("patcher");
        assertThat(result.requestTime()).isNotNull();
    }

    @Test
    void createPatchCommand() {
        patchRequest.setId("asset-3");

        PatchAssetCommand result = registry.createPatchCommand(AssetType.SHIP, patchRequest);

        assertThat(result.assetId()).isEqualTo("asset-3");
        assertThat(result.attributes()).containsExactly(
                new AVString("name", "Sea Queen"),
                new AVDecimal("imo", new BigDecimal("9876543")),
                new AVBoolean("active", false)
        );
        assertThat(result.executedBy()).isEqualTo("patcher");
        assertThat(result.requestTime()).isNotNull();
    }

    @Test
    void createPatchCommandWithoutRequestId() {
        AssetPatchRequest withoutId = new AssetPatchRequest();
        withoutId.setAttributes(objectMapper.createObjectNode());
        withoutId.setExecutedBy("tester");

        assertThatThrownBy(() -> registry.createPatchCommand(AssetType.CRE, withoutId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("asset id");
    }

    @Test
    void createDeleteCommand() {
        AssetDeleteRequest request = new AssetDeleteRequest("asset-33", "deleter");

        DeleteAssetCommand command = registry.createDeleteCommand("asset-33", request);

        assertThat(command.assetId()).isEqualTo("asset-33");
        assertThat(command.executedBy()).isEqualTo("deleter");
        assertThat(command.requestTime()).isNotNull();

        assertThatThrownBy(() -> registry.createDeleteCommand("asset-33", new AssetDeleteRequest("other", "deleter")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("match");
    }

    private ValidationRuleFactory ruleFactory(CustomValidationRuleRegistry customRegistry) {
        return new ValidationRuleFactory(customRegistry);
    }

    private ObjectNode createAttributesNode() {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("city", "Frankfurt");
        node.put("area", new BigDecimal("500.25"));
        node.put("active", true);
        return node;
    }

    private ObjectNode patchAttributesNode() {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("name", "Sea Queen");
        node.put("imo", new BigDecimal("9876543"));
        node.put("active", false);
        return node;
    }

    private static final class FixedRegistry implements AttributeDefinitionRegistry {

        private final Map<AssetType, Map<String, AttributeDefinition>> definitions = new HashMap<>();
        private final Map<AssetType, Map<String, List<ConstraintDefinition>>> constraints = new HashMap<>();

        private FixedRegistry() {
            var city = new AttributeDefinition(AssetType.CRE, "city", AttributeType.STRING, false);
            var area = new AttributeDefinition(AssetType.CRE, "area", AttributeType.DECIMAL, false);
            var activeCre = new AttributeDefinition(AssetType.CRE, "active", AttributeType.BOOLEAN, false);
            var name = new AttributeDefinition(AssetType.SHIP, "name", AttributeType.STRING, true);
            var imo = new AttributeDefinition(AssetType.SHIP, "imo", AttributeType.DECIMAL, false);
            var activeShip = new AttributeDefinition(AssetType.SHIP, "active", AttributeType.BOOLEAN, false);

            definitions.put(AssetType.CRE, Map.of(
                    "city", city,
                    "area", area,
                    "active", activeCre
            ));
            constraints.put(AssetType.CRE, Map.of(
                    "city", List.of(new ConstraintDefinition(city, ConstraintDefinition.Rule.TYPE, null)),
                    "area", List.of(new ConstraintDefinition(area, ConstraintDefinition.Rule.TYPE, null)),
                    "active", List.of(new ConstraintDefinition(activeCre, ConstraintDefinition.Rule.TYPE, null))
            ));
            definitions.put(AssetType.SHIP, Map.of(
                    "name", name,
                    "imo", imo,
                    "active", activeShip
            ));
            constraints.put(AssetType.SHIP, Map.of(
                    "name", List.of(new ConstraintDefinition(name, ConstraintDefinition.Rule.TYPE, null)),
                    "imo", List.of(new ConstraintDefinition(imo, ConstraintDefinition.Rule.TYPE, null)),
                    "active", List.of(new ConstraintDefinition(activeShip, ConstraintDefinition.Rule.TYPE, null))
            ));
        }

        @Override
        public Map<String, AttributeDefinition> getDefinitions(AssetType type) {
            return definitions.getOrDefault(type, Map.of());
        }

        @Override
        public Map<String, List<ConstraintDefinition>> getConstraints(AssetType type) {
            return constraints.getOrDefault(type, Map.of());
        }

        @Override
        public void refresh() {
            // no-op
        }
    }
}
