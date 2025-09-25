package com.db.assetstore.domain.service.startup;

import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.db.assetstore.domain.service.type.TypeSchemaRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttributeBootstrapService {

    private final AttributeDefinitionRegistry attributeDefinitionRegistry;
    private final TypeSchemaRegistry typeSchemaRegistry;

    @Transactional
    public void bootstrap() {
        typeSchemaRegistry.rebuild();
        attributeDefinitionRegistry.refresh();
    }
}

