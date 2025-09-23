package com.db.assetstore.service;

import com.db.assetstore.domain.schema.TypeSchemaRegistry;

import com.db.assetstore.AssetType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TypeSchemaRegistryTest {

    @Test
    void discoversSupportedTypesBasedOnSchemas() {
        TypeSchemaRegistry reg = new TypeSchemaRegistry();
        reg.discover();

        Set<AssetType> supported = reg.supportedTypes();

        assertTrue(supported.contains(AssetType.CRE), "CRE should be supported (schema present)");
        assertTrue(supported.contains(AssetType.SHIP), "SHIP should be supported after adding schema");

        assertFalse(supported.contains(AssetType.AV), "AV should not be supported without schema");
        assertFalse(supported.contains(AssetType.SPV), "SPV should not be supported without schema");
    }
}
