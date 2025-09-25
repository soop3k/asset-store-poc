package com.db.assetstore.domain.service.type;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.type.AttributeType;
import com.db.assetstore.infra.jpa.AttributeDefEntity;
import com.db.assetstore.infra.repository.AttributeDefRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class AttributeDefinitionsProvider {
    private final AttributeDefRepository defRepo;
    private final AttributeDefinitionRegistry attributeDefinitionRegistry;
    private final TypeSchemaRegistry typeSchemaRegistry;

    public Map<String, AttributeDefEntity> resolve(AssetType type) {
        if (type == null) {
            return Collections.emptyMap();
        }

        boolean hasSchema = typeSchemaRegistry.getSchemaPath(type).isPresent();
        if (hasSchema) {
            Map<String, AttributeDefinitionRegistry.Def> regDefs =
                    attributeDefinitionRegistry.getDefinitions(type);
            Map<String, AttributeDefEntity> tmp = new HashMap<>();
            for (var entry : regDefs.entrySet()) {
                var d = entry.getValue();
                tmp.put(entry.getKey(),
                        new AttributeDefEntity(type, d.name(), toAttrType(d.valueType()), d.required()));
            }
            return tmp;
        }
        List<AttributeDefEntity> defs = defRepo.findAllByType(type);
        Map<String, AttributeDefEntity> map = new HashMap<>();
        for (AttributeDefEntity d : defs) {
            map.put(d.getName(), d);
        }
        return map;
    }

    private static AttributeType toAttrType(
            AttributeDefinitionRegistry.ValueType vt) {
        if (vt == null) {
            return AttributeType.STRING;
        }
        return switch (vt) {
            case STRING -> AttributeType.STRING;
            case DECIMAL -> AttributeType.DECIMAL;
            case BOOLEAN -> AttributeType.BOOLEAN;
        };
    }
}
