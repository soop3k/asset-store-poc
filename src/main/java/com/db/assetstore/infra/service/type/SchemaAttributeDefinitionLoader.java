package com.db.assetstore.infra.service.type;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.type.AttributeType;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry.AttributeDefinition;
import com.db.assetstore.domain.service.type.TypeSchemaRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaAttributeDefinitionLoader {

    private final TypeSchemaRegistry typeSchemaRegistry;

    public Optional<Map<String, AttributeDefinition>> load(AssetType assetType) {
        return typeSchemaRegistry.getSchemaNode(assetType)
                .map(node -> Collections.unmodifiableMap(parseSchema(assetType, node)));
    }

    private Map<String, AttributeDefinition> parseSchema(AssetType assetType, JsonNode schema) {
        Map<String, AttributeDefinition> definitions = new LinkedHashMap<>();
        if (schema == null || !schema.isObject()) {
            log.debug("Schema for {} is not an object node", assetType);
            return definitions;
        }

        Set<String> required = readRequired(schema.get("required"));
        JsonNode properties = schema.get("properties");
        if (properties == null || !properties.isObject()) {
            return definitions;
        }

        Iterator<String> fieldNames = properties.fieldNames();
        while (fieldNames.hasNext()) {
            String name = fieldNames.next();
            JsonNode definitionNode = properties.get(name);
            AttributeType attributeType = readAttributeType(definitionNode);
            definitions.put(name, new AttributeDefinition(name, attributeType, required.contains(name)));
        }

        return definitions;
    }

    private static Set<String> readRequired(JsonNode requiredNode) {
        if (requiredNode == null || !requiredNode.isArray()) {
            return Collections.emptySet();
        }
        Set<String> required = new HashSet<>();
        requiredNode.forEach(node -> {
            if (node.isTextual()) {
                required.add(node.asText());
            }
        });
        return required;
    }

    private static AttributeType readAttributeType(JsonNode definitionNode) {
        if (definitionNode == null || !definitionNode.isObject()) {
            return AttributeType.STRING;
        }
        JsonNode typeNode = definitionNode.get("type");
        if (typeNode == null) {
            return AttributeType.STRING;
        }
        return switch (typeNode.asText()) {
            case "integer", "number" -> AttributeType.DECIMAL;
            case "boolean" -> AttributeType.BOOLEAN;
            case "string" -> AttributeType.STRING;
            default -> AttributeType.STRING;
        };
    }
}

