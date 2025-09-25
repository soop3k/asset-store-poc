package com.db.assetstore.infra.service.type;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.type.AttributeType;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.db.assetstore.domain.service.type.TypeSchemaRegistry;
import com.db.assetstore.infra.jpa.AttributeDefEntity;
import com.db.assetstore.infra.repository.AttributeDefRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

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
        attributeDefRepository.save(new AttributeDefEntity(AssetType.CRE, "city", AttributeType.DECIMAL, false));
        attributeDefRepository.save(new AttributeDefEntity(AssetType.CRE, "custom", AttributeType.STRING, true));

        Map<String, AttributeDefinitionRegistry.AttributeDefinition> definitions =
                registry.getDefinitions(AssetType.CRE);

        assertThat(definitions).containsKeys("city", "area", "rooms", "active", "custom");
        assertThat(definitions.get("city").attributeType()).isEqualTo(AttributeType.STRING);
        assertThat(definitions.get("city").required()).isTrue();
        assertThat(definitions.get("custom").attributeType()).isEqualTo(AttributeType.STRING);
        assertThat(definitions.get("custom").required()).isTrue();
    }

    @Test
    void fallsBackToDatabaseWhenSchemaIsMissing() {
        attributeDefRepository.save(new AttributeDefEntity(AssetType.AV, "tailNumber", AttributeType.STRING, true));
        attributeDefRepository.save(new AttributeDefEntity(AssetType.AV, "manufacturer", AttributeType.STRING, false));

        Map<String, AttributeDefinitionRegistry.AttributeDefinition> definitions =
                registry.getDefinitions(AssetType.AV);

        assertThat(definitions).containsOnlyKeys("tailNumber", "manufacturer");
        assertThat(definitions.get("tailNumber").required()).isTrue();
    }

    @Test
    void refreshReloadsDefinitionsAfterDatabaseChanges() {
        attributeDefRepository.save(new AttributeDefEntity(AssetType.AV, "tailNumber", AttributeType.STRING, true));

        Map<String, AttributeDefinitionRegistry.AttributeDefinition> initial =
                registry.getDefinitions(AssetType.AV);
        assertThat(initial).containsOnlyKeys("tailNumber");

        attributeDefRepository.save(new AttributeDefEntity(AssetType.AV, "manufacturer", AttributeType.STRING, false));

        Map<String, AttributeDefinitionRegistry.AttributeDefinition> cached =
                registry.getDefinitions(AssetType.AV);
        assertThat(cached).containsOnlyKeys("tailNumber");

        registry.refresh();

        Map<String, AttributeDefinitionRegistry.AttributeDefinition> refreshed =
                registry.getDefinitions(AssetType.AV);
        assertThat(refreshed).containsOnlyKeys("tailNumber", "manufacturer");
    }
}

