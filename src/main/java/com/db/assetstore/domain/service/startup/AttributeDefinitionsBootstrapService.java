package com.db.assetstore.domain.service.startup;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.type.AttributeType;
import com.db.assetstore.infra.jpa.AttributeDefEntity;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.db.assetstore.infra.repository.AttributeDefRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.db.assetstore.domain.service.type.TypeSchemaRegistry;


@Service
@RequiredArgsConstructor
public class AttributeDefinitionsBootstrapService {
    private static final Logger log = LoggerFactory.getLogger(AttributeDefinitionsBootstrapService.class);

    private final AttributeDefRepository repository;
    private final AttributeDefinitionRegistry attributeDefinitionRegistry;
    private final TypeSchemaRegistry typeSchemaRegistry;

    @Transactional
    public void bootstrap() {
        var types = typeSchemaRegistry.supportedTypes();

        for (AssetType t : types) {
            var defs = attributeDefinitionRegistry.getDefinitions(t);

            for (var def : defs.values()) {
                if (!repository.existsByTypeAndName(t, def.name())) {
                    var e = new AttributeDefEntity(
                            t,
                            def.name(),
                            toAttrType(def.valueType()),
                            def.required()
                    );
                    repository.save(e);
                }
            }
        }

        log.info("Bootstrapped attribute definitions from JSON Schemas to DB");
    }

    private static AttributeType toAttrType(AttributeDefinitionRegistry.ValueType vt) {
        if (vt == null) return AttributeType.STRING;
        return switch (vt) {
            case STRING -> AttributeType.STRING;
            case DECIMAL -> AttributeType.DECIMAL;
            case BOOLEAN -> AttributeType.BOOLEAN;
        };
    }
}

