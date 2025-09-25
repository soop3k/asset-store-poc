package com.db.assetstore.domain.service.startup;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.db.assetstore.domain.service.type.TypeSchemaRegistry;
import com.db.assetstore.infra.jpa.AttributeDefEntity;
import com.db.assetstore.infra.repository.AttributeDefRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AttributeBootstrapService {
    private static final Logger log = LoggerFactory.getLogger(AttributeBootstrapService.class);

    private final AttributeDefRepository repository;
    private final AttributeDefinitionRegistry attributeDefinitionRegistry;
    private final TypeSchemaRegistry typeSchemaRegistry;

    @Transactional
    public void bootstrap() {
        var types = typeSchemaRegistry.rebuild();
        attributeDefinitionRegistry.rebuild();

        for (AssetType t : types) {
            var defs = attributeDefinitionRegistry.getDefinitions(t);

            for (var def : defs.values()) {
                if (!repository.existsByTypeAndName(t, def.name())) {
                    var e = new AttributeDefEntity(
                            t,
                            def.name(),
                            AttributeDefinitionRegistry.toAttributeType(def.valueType()),
                            def.required()
                    );
                    repository.save(e);
                }
                log.info("Bootstrapped attribute from schemas {type={}, count={}}", t, defs.size());
            }
        }
    }
}

