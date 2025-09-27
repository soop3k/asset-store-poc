package com.db.assetstore.service;

import com.db.assetstore.domain.service.type.TypeSchemaRegistry;

import com.db.assetstore.AssetType;
import com.db.assetstore.infra.config.JsonMapperProvider;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TypeSchemaRegistryTest {

    @Test
    void discoversSupportedTypesBasedOnSchemas() {
        TypeSchemaRegistry reg = new TypeSchemaRegistry(new JsonMapperProvider().objectMapper());
        reg.discover();

        Set<AssetType> supported = reg.supportedTypes();

        assertTrue(supported.contains(AssetType.CRE), "CRE should be supported");
        assertTrue(supported.contains(AssetType.SHIP), "SHIP should be supported");

        assertFalse(supported.contains(AssetType.AV), "AV should not be supported without schema");
        assertTrue(supported.contains(AssetType.SPV), "SPV should be supported");
    }
}
