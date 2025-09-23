package com.db.assetstore.json;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AssetJsonFactory;
import com.db.assetstore.domain.json.AttributeJsonReader;
import com.db.assetstore.domain.schema.TypeSchemaRegistry;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.db.assetstore.domain.service.validation.AssetAttributeValidationService;
import com.db.assetstore.domain.service.validation.AssetTypeValidator;
import com.db.assetstore.infra.config.JsonMapperProvider;
import com.db.assetstore.infra.mapper.AssetMapper;
import com.db.assetstore.infra.mapper.AttributeMapper;
import com.db.assetstore.infra.repository.AssetRepository;
import com.db.assetstore.infra.repository.AttributeRepository;
import com.db.assetstore.infra.repository.CommandLogRepository;
import com.db.assetstore.infra.service.AssetCommandServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class AssetJsonFactoryTypeSchemaIntegrationTest {

    private static final ObjectMapper M = new JsonMapperProvider().objectMapper();
    private final TypeSchemaRegistry typeSchema = new TypeSchemaRegistry();
    private final AttributeDefinitionRegistry registry = new AttributeDefinitionRegistry(M, typeSchema);
    private final AttributeJsonReader reader = new AttributeJsonReader(M, registry);
    private final AssetJsonFactory factory = new AssetJsonFactory(M, reader, registry);
    private final AssetTypeValidator typeValidator = new AssetTypeValidator(typeSchema, M);
    private final AssetAttributeValidationService validator = new AssetAttributeValidationService(factory, typeValidator);

    @BeforeEach
    void rebuild() {
        typeSchema.discover();
        registry.rebuild();
    }

    @Test
    void addAssetFromJsonRejectsTypeWithoutSchemaViaService() {
        String json = "{\"type\":\"SPV\",\"id\":\"spv-1\"}";
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> validator.validateJson(json));
        assertTrue(ex.getMessage().toLowerCase().contains("schema"));
    }

    @Test
    void addAssetFromJsonForTypeAllowsAdditionalUnknownAttributes() {
        String attrs = "{" +
                "\"type\":\"CRE\"," +
                "\"id\":\"cre-2\"," +
                "\"city\":\"Gdansk\"," +
                "\"unknownAttr\":123" +
                "}";
        var assetRepo = Mockito.mock(AssetRepository.class);
        Mockito.when(assetRepo.save(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));
        var attrRepo = Mockito.mock(AttributeRepository.class);
        var assetMapper = Mappers.getMapper(AssetMapper.class);
        var attributeMapper = Mockito.mock(AttributeMapper.class,
                Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS));

        validator.validateJson(attrs);
        var commandLogRepository = Mockito.mock(CommandLogRepository.class);
        Mockito.when(commandLogRepository.save(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));
        var command = new AssetCommandServiceImpl(assetMapper, attributeMapper, assetRepo, attrRepo, commandLogRepository, M);
        String id = command.create(factory.fromJson(attrs));
        assertEquals("cre-2", id);
    }
}
