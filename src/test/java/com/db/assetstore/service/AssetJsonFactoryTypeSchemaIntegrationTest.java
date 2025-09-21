package com.db.assetstore.service;

import com.db.assetstore.json.AssetJsonFactory;

import com.db.assetstore.AssetType;
import com.db.assetstore.model.Asset;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class AssetJsonFactoryTypeSchemaIntegrationTest {

    private final AssetJsonFactory factory = new AssetJsonFactory();

    @Test
    void addAssetFromJsonRejectsTypeWithoutSchema_viaService() {
        // SPV is in enum but there is no schemas/SPV.schema.json; should be rejected now
        String json = "{\"type\":\"SPV\",\"id\":\"spv-1\"}";
        var repo = Mockito.mock(com.db.assetstore.repository.AssetRepository.class);
        var svc = new DefaultAssetService(repo);
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
        var repo = Mockito.mock(com.db.assetstore.repository.AssetRepository.class);
        var svc = new DefaultAssetService(repo);
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
        var repo = Mockito.mock(com.db.assetstore.repository.AssetRepository.class);
        Mockito.when(repo.saveAsset(any(Asset.class))).thenReturn("id-x");
        var svc = new DefaultAssetService(repo);

        String id = svc.addAssetFromJson(AssetType.CRE, attrs);
        assertEquals("id-x", id);
    }
}
