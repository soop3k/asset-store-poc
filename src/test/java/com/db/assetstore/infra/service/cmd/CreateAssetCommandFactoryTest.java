package com.db.assetstore.infra.service.cmd;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.model.type.AttributeType;
import com.db.assetstore.domain.service.cmd.CreateAssetCommand;
import com.db.assetstore.domain.service.cmd.factory.CreateAssetCommandFactory;
import com.db.assetstore.domain.service.type.AttributeDefinition;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.db.assetstore.domain.service.type.ConstraintDefinition;
import com.db.assetstore.domain.service.validation.AttributeValidator;
import com.db.assetstore.domain.service.validation.custom.MatchingAttributesRule;
import com.db.assetstore.domain.service.validation.rule.CustomValidationRuleRegistry;
import com.db.assetstore.domain.service.validation.rule.ValidationRuleFactory;
import com.db.assetstore.infra.api.dto.AssetCreateRequest;
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

class CreateAssetCommandFactoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private CreateAssetCommandFactory factory;
    private AttributeDefinitionRegistry registry;
    private CustomValidationRuleRegistry customRegistry;

    @BeforeEach
    void setUp() {
        registry = new FixedRegistry();
        customRegistry = new CustomValidationRuleRegistry(List.of(new MatchingAttributesRule()));
        var validator = new AttributeValidator(registry, ruleFactory(customRegistry));
        AttributeJsonReader reader = new AttributeJsonReader(objectMapper, registry);
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

    private static final class FixedRegistry implements AttributeDefinitionRegistry {

        private final Map<AssetType, Map<String, AttributeDefinition>> definitions = new HashMap<>();
        private final Map<AssetType, Map<String, List<ConstraintDefinition>>> constraints = new HashMap<>();

        private FixedRegistry() {
            var city = new AttributeDefinition(AssetType.CRE, "city", AttributeType.STRING, false);
            var area = new AttributeDefinition(AssetType.CRE, "area", AttributeType.DECIMAL, false);
            var active = new AttributeDefinition(AssetType.CRE, "active", AttributeType.BOOLEAN, false);

            definitions.put(AssetType.CRE, Map.of(
                    "city", city,
                    "area", area,
                    "active", active
            ));
            constraints.put(AssetType.CRE, Map.of(
                    "city", List.of(new ConstraintDefinition(city, ConstraintDefinition.Rule.TYPE, null)),
                    "area", List.of(new ConstraintDefinition(area, ConstraintDefinition.Rule.TYPE, null)),
                    "active", List.of(new ConstraintDefinition(active, ConstraintDefinition.Rule.TYPE, null))
            ));
            definitions.put(AssetType.SHIP, Map.of());
            constraints.put(AssetType.SHIP, Map.of());
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
