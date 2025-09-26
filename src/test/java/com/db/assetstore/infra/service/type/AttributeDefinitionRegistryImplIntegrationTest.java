package com.db.assetstore.infra.service.type;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.type.AttributeType;
import com.db.assetstore.domain.service.type.AttributeDefinition;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.db.assetstore.domain.service.type.ConstraintDefinition;
import com.db.assetstore.domain.service.type.TypeSchemaRegistry;
import com.db.assetstore.infra.jpa.AttributeDefEntity;
import com.db.assetstore.infra.jpa.ConstraintDefEntity;
import com.db.assetstore.infra.repository.AttributeDefRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AttributeDefinitionRegistryImplIntegrationTest {

    @Autowired
    private AttributeDefinitionRegistry registry;

    @Autowired
    private AttributeDefRepository attributeDefRepository;

    @Autowired
    private TypeSchemaRegistry typeSchemaRegistry;

    @BeforeEach
    void setUp() {
        typeSchemaRegistry.rebuild();
        registry.refresh();
        attributeDefRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        attributeDefRepository.deleteAll();
        registry.refresh();
    }

    @Test
    void schemaDefinitionsOverrideDatabaseValues() {
        AttributeDefEntity city = new AttributeDefEntity(AssetType.CRE, "city", AttributeType.DECIMAL, false);
        city.getConstraints().add(new ConstraintDefEntity(city, "CUSTOM", "{\"class\":\"MatchingAttributesRule\"}"));
        attributeDefRepository.save(city);
        attributeDefRepository.save(new AttributeDefEntity(AssetType.CRE, "custom", AttributeType.STRING, true));

        Map<String, AttributeDefinition> definitions = registry.getDefinitions(AssetType.CRE);
        Map<String, List<ConstraintDefinition>> constraints = registry.getConstraints(AssetType.CRE);

        assertThat(definitions).containsKeys("city", "area", "rooms", "active", "custom");
        AttributeDefinition cityDef = definitions.get("city");
        assertThat(cityDef.attributeType()).isEqualTo(AttributeType.STRING);
        assertThat(constraints.getOrDefault("city", List.of()))
                .anyMatch(c -> c.rule() == ConstraintDefinition.Rule.REQUIRED);
        assertThat(constraints.getOrDefault("city", List.of()))
                .anyMatch(c -> c.rule() == ConstraintDefinition.Rule.CUSTOM);

        AttributeDefinition customDef = definitions.get("custom");
        assertThat(customDef.attributeType()).isEqualTo(AttributeType.STRING);
        assertThat(constraints.getOrDefault("custom", List.of()))
                .anyMatch(c -> c.rule() == ConstraintDefinition.Rule.REQUIRED);
    }

    @Test
    void fallsBackToDatabaseWhenSchemaIsMissing() {
        attributeDefRepository.save(new AttributeDefEntity(AssetType.AV, "tailNumber", AttributeType.STRING, true));
        attributeDefRepository.save(new AttributeDefEntity(AssetType.AV, "manufacturer", AttributeType.STRING, false));

        Map<String, AttributeDefinition> definitions = registry.getDefinitions(AssetType.AV);
        Map<String, List<ConstraintDefinition>> constraints = registry.getConstraints(AssetType.AV);

        assertThat(definitions).containsOnlyKeys("tailNumber", "manufacturer");
        assertThat(constraints.getOrDefault("tailNumber", List.of()))
                .anyMatch(c -> c.rule() == ConstraintDefinition.Rule.REQUIRED);
    }

    @Test
    void refreshReloadsDefinitionsAfterDatabaseChanges() {
        attributeDefRepository.save(new AttributeDefEntity(AssetType.AV, "tailNumber", AttributeType.STRING, true));

        Map<String, AttributeDefinition> initial = registry.getDefinitions(AssetType.AV);
        assertThat(initial).containsOnlyKeys("tailNumber");

        attributeDefRepository.save(new AttributeDefEntity(AssetType.AV, "manufacturer", AttributeType.STRING, false));

        Map<String, AttributeDefinition> cached = registry.getDefinitions(AssetType.AV);
        assertThat(cached).containsOnlyKeys("tailNumber");

        registry.refresh();

        Map<String, AttributeDefinition> refreshed = registry.getDefinitions(AssetType.AV);
        assertThat(refreshed).containsOnlyKeys("tailNumber", "manufacturer");
    }
}

