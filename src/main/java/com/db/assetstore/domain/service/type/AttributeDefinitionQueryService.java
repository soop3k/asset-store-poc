package com.db.assetstore.domain.service.type;

import com.db.assetstore.AssetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttributeDefinitionQueryService {

    private final AttributeDefinitionRegistry attributeDefinitionRegistry;

    public List<AttributeDefinitionRegistry.AttributeDefinition> listDefinitions(AssetType assetType) {
        return new ArrayList<>(attributeDefinitionRegistry.getDefinitions(assetType).values());
    }
}

