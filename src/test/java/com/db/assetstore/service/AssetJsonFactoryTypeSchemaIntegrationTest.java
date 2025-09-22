package com.db.assetstore.service;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.json.AssetJsonFactory;
import com.db.assetstore.infra.mapper.AssetMapper;
import com.db.assetstore.infra.mapper.AttributeHistoryMapper;
import com.db.assetstore.infra.mapper.AttributeMapper;
import com.db.assetstore.infra.repository.AssetRepository;
import com.db.assetstore.infra.repository.AttributeDefRepository;
import com.db.assetstore.infra.repository.AttributeHistoryRepository;
import com.db.assetstore.infra.repository.AttributeRepository;
import com.db.assetstore.infra.service.search.AssetSearchSpecificationService;
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
        var assetRepo = Mockito.mock(AssetRepository.class);
        var attrRepo = Mockito.mock(AttributeRepository.class);
        var defRepo = Mockito.mock(AttributeDefRepository.class);
        var historyRepo = Mockito.mock(AttributeHistoryRepository.class);
        var assetMapper = Mappers.getMapper(AssetMapper.class);
        var attributeMapper = Mockito.mock(AttributeMapper.class,
                Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS));
        var specService = new AssetSearchSpecificationService();
        var historyMapper = Mappers.getMapper(AttributeHistoryMapper.class);
        var svc = new com.db.assetstore.infra.service.AssetService(assetMapper, attributeMapper, assetRepo, attrRepo, defRepo, historyRepo, specService, historyMapper);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> svc.addAssetFromJson(json));
        assertTrue(ex.getMessage().toLowerCase().contains("schema"));
    }

    @Test
    void addAssetFromJsonForType_validatesAttributesAgainstSchema_andFailsOnMissingRequired() {
        // CRE schema requires 'city'
        String attrs = "{" +
                "\"id\":\"cre-1\"," +
                "\"rooms\":2" + // missing required city
                "}";
        var assetRepo = Mockito.mock(AssetRepository.class);
        var attrRepo = Mockito.mock(AttributeRepository.class);
        var defRepo = Mockito.mock(AttributeDefRepository.class);
        var historyRepo = Mockito.mock(AttributeHistoryRepository.class);
        var assetMapper = Mappers.getMapper(AssetMapper.class);
        var attributeMapper = Mockito.mock(AttributeMapper.class,
                Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS));
        var specService = new AssetSearchSpecificationService();
        var historyMapper = Mappers.getMapper(AttributeHistoryMapper.class);
        var svc = new com.db.assetstore.infra.service.AssetService(assetMapper, attributeMapper, assetRepo, attrRepo, defRepo, historyRepo, specService, historyMapper);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> svc.addAssetFromJson(AssetType.CRE, attrs));
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
        // Service returns ID from saved entity
        Mockito.when(assetRepo.save(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));
        var attrRepo = Mockito.mock(AttributeRepository.class);
        var defRepo = Mockito.mock(AttributeDefRepository.class);
        var historyRepo = Mockito.mock(AttributeHistoryRepository.class);
        var assetMapper = Mappers.getMapper(AssetMapper.class);
        var attributeMapper = Mockito.mock(AttributeMapper.class,
                Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS));
        var specService = new AssetSearchSpecificationService();
        var historyMapper = Mappers.getMapper(AttributeHistoryMapper.class);
        var svc = new com.db.assetstore.infra.service.AssetService(assetMapper, attributeMapper, assetRepo, attrRepo, defRepo, historyRepo, specService, historyMapper);

        String id = svc.addAssetFromJson(AssetType.CRE, attrs);
        assertEquals("cre-2", id);
    }
}
