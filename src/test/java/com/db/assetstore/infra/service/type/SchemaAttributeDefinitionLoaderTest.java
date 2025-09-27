package com.db.assetstore.infra.service.type;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.service.type.AttributeDefinitionLoader;
import com.db.assetstore.domain.service.type.ConstraintDefinition;
import com.db.assetstore.domain.service.type.TypeSchemaRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaAttributeDefinitionLoaderTest {

    private SchemaAttributeDefinitionLoader loader;
    private TypeSchemaRegistry typeSchemaRegistry;

    @BeforeEach
    void setUp() {
        typeSchemaRegistry = new TypeSchemaRegistry(new ObjectMapper());
        typeSchemaRegistry.rebuild();
        loader = new SchemaAttributeDefinitionLoader(typeSchemaRegistry);
    }

    @Test
    void loadsCustomRulesFromSchema() throws Exception {
        AttributeDefinitionLoader.AttributeDefinitions definitions = loader.load(AssetType.SPV);

        List<ConstraintDefinition> constraints = definitions.constraints()
                .getOrDefault("primary", List.of());

        assertThat(constraints)
                .anySatisfy(constraint -> {
                    assertThat(constraint.rule()).isEqualTo(ConstraintDefinition.Rule.CUSTOM);
                    assertThat(constraint.value()).isEqualTo("matchingAttributes");
                });
    }
}
