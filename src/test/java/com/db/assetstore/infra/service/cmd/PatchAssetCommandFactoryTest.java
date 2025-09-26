package com.db.assetstore.infra.service.cmd;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.model.type.AttributeType;
import com.db.assetstore.domain.service.cmd.PatchAssetCommand;
import com.db.assetstore.domain.service.cmd.factory.PatchAssetCommandFactory;
import com.db.assetstore.domain.service.type.AttributeDefinition;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.db.assetstore.domain.service.type.ConstraintDefinition;
import com.db.assetstore.domain.service.validation.AttributeValidator;
import com.db.assetstore.domain.service.validation.custom.MatchingAttributesRule;
import com.db.assetstore.domain.service.validation.rule.CustomRule;
import com.db.assetstore.domain.service.validation.rule.CustomValidationRuleRegistry;
import com.db.assetstore.domain.service.validation.rule.EnumRule;
import com.db.assetstore.domain.service.validation.rule.LengthRule;
import com.db.assetstore.domain.service.validation.rule.MinMaxRule;
import com.db.assetstore.domain.service.validation.rule.RequiredRule;
import com.db.assetstore.domain.service.validation.rule.TypeRule;
import com.db.assetstore.domain.service.validation.rule.ValidationRuleRegistry;
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

class PatchAssetCommandFactoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private PatchAssetCommandFactory factory;
    private AssetPatchRequest request;
    private AttributeDefinitionRegistry registry;
    private ValidationRuleRegistry ruleRegistry;
    private CustomValidationRuleRegistry customRegistry;

    @BeforeEach
    void setUp() {
        registry = new FixedRegistry();
        customRegistry = new CustomValidationRuleRegistry(List.of(new MatchingAttributesRule()));
        ruleRegistry = ruleRegistry(customRegistry);
        AttributeValidator validator = new AttributeValidator(registry, ruleRegistry);
        AttributeJsonReader reader = new AttributeJsonReader(objectMapper, registry);
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

    private ValidationRuleRegistry ruleRegistry(CustomValidationRuleRegistry customRegistry) {
        return new ValidationRuleRegistry(List.of(
                new TypeRule(),
                new RequiredRule(),
                new MinMaxRule(),
                new EnumRule(),
                new LengthRule(),
                new CustomRule(customRegistry)
        ));
    }

    private ObjectNode attributesNode(String name, BigDecimal imo, boolean active) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("name", name);
        node.put("imo", imo);
        node.put("active", active);
        return node;
    }

    private static final class FixedRegistry implements AttributeDefinitionRegistry {

        private final Map<AssetType, Map<String, AttributeDefinition>> definitions = new HashMap<>();
        private final Map<AssetType, Map<String, List<ConstraintDefinition>>> constraints = new HashMap<>();

        private FixedRegistry() {
            AttributeDefinition name = new AttributeDefinition(AssetType.SHIP, "name", AttributeType.STRING);
            AttributeDefinition imo = new AttributeDefinition(AssetType.SHIP, "imo", AttributeType.DECIMAL);
            AttributeDefinition active = new AttributeDefinition(AssetType.SHIP, "active", AttributeType.BOOLEAN);

            definitions.put(AssetType.SHIP, Map.of(
                    "name", name,
                    "imo", imo,
                    "active", active
            ));
            constraints.put(AssetType.SHIP, Map.of(
                    "name", List.of(new ConstraintDefinition(name, ConstraintDefinition.Rule.TYPE, null)),
                    "imo", List.of(new ConstraintDefinition(imo, ConstraintDefinition.Rule.TYPE, null)),
                    "active", List.of(new ConstraintDefinition(active, ConstraintDefinition.Rule.TYPE, null))
            ));
            definitions.put(AssetType.CRE, Map.of());
            constraints.put(AssetType.CRE, Map.of());
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
