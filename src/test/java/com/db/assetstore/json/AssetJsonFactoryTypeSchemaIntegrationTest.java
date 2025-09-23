package com.db.assetstore.json;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AssetJsonFactory;
import com.db.assetstore.domain.service.validation.AssetAttributeValidationService;
import com.db.assetstore.infra.mapper.AssetMapper;
import com.db.assetstore.infra.mapper.AttributeMapper;
import com.db.assetstore.infra.repository.AssetRepository;
import com.db.assetstore.infra.repository.AttributeRepository;
import com.db.assetstore.infra.service.AssetCommandServiceImpl;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class AssetJsonFactoryTypeSchemaIntegrationTest {

    private final AssetJsonFactory factory = new AssetJsonFactory();

    @Test
    void addAssetFromJsonRejectsTypeWithoutSchema_viaService() {
        // SPV is in enum but there is no schemas/SPV.schema.json; should be rejected now
        String json = "{\"type\":\"SPV\",\"id\":\"spv-1\"}";
        var validator = new AssetAttributeValidationService();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> validator.validateEnvelope(json));
        assertTrue(ex.getMessage().toLowerCase().contains("schema"));
    }

    @Test
    void addAssetFromJsonForType_validatesAttributesAgainstSchema_andFailsOnMissingRequired() {
        // CRE schema requires 'city'
        String attrs = "{" +
                "\"id\":\"cre-1\"," +
                "\"rooms\":2" + // missing required city
                "}";
        var validator = new AssetAttributeValidationService();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validateForType(AssetType.CRE, attrs));
        assertTrue(ex.getMessage().toLowerCase().contains("validation"), ex.getMessage());
    }

    @Test
    void addAssetFromJsonForType_allowsAdditionalUnknownAttributes() {
        // CRE schema doesn't define 'unknownAttr' but additional properties are ignored by validator
        String attrs = "{" +
                "\"id\":\"cre-2\"," +
                "\"city\":\"Gdansk\"," +
                "\"unknownAttr\":123" +
                "}";
        var assetRepo = Mockito.mock(AssetRepository.class);
        // Command service returns ID from saved entity
        Mockito.when(assetRepo.save(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));
        var attrRepo = Mockito.mock(AttributeRepository.class);
        var assetMapper = Mappers.getMapper(AssetMapper.class);
        var attributeMapper = Mockito.mock(AttributeMapper.class,
                Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS));

        // Validate should succeed
        new AssetAttributeValidationService().validateForType(AssetType.CRE, attrs);
        // Create via command service
        var command = new AssetCommandServiceImpl(assetMapper, attributeMapper, assetRepo, attrRepo);
        String id = command.create(factory.fromJsonForType(AssetType.CRE, attrs)).id();
        assertEquals("cre-2", id);
    }
}
