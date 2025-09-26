package com.db.assetstore.infra.json;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.domain.model.type.AVBoolean;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.model.type.AttributeType;
import com.db.assetstore.domain.service.type.AttributeDefinition;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class AttributeJsonReader {

    private final ObjectMapper objectMapper;
    private final AttributeDefinitionRegistry attributeDefinitionRegistry;

    public AttributeJsonReader(ObjectMapper objectMapper,
                               AttributeDefinitionRegistry attributeDefinitionRegistry) {
        this.objectMapper = objectMapper;
        this.attributeDefinitionRegistry = attributeDefinitionRegistry;
    }

    public AttributesCollection read(AssetType type, JsonNode jsonNode) {
        if (jsonNode == null || jsonNode.isNull()) {
            return AttributesCollection.empty();
        }
        if (!jsonNode.isObject()) {
            throw new IllegalArgumentException("Attributes payload must be a JSON object");
        }
        Map<String, AttributeDefinition> definitions = attributeDefinitionRegistry.getDefinitions(type);
        List<AttributeValue<?>> values = new ArrayList<>();
        Iterator<String> names = jsonNode.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            JsonNode valueNode = jsonNode.get(name);
            AttributeDefinition definition = definitions.get(name);
            if (valueNode == null || valueNode.isNull()) {
                values.add(createAttributeValue(name, definition, null));
                continue;
            }
            if (valueNode.isArray()) {
                for (JsonNode item : valueNode) {
                    values.add(createAttributeValue(name, definition, item));
                }
            } else {
                values.add(createAttributeValue(name, definition, valueNode));
            }
        }
        return AttributesCollection.fromFlat(values);
    }

    private AttributeValue<?> createAttributeValue(String name,
                                                   AttributeDefinition definition,
                                                   JsonNode node) {
        AttributeType type = definition == null ? inferType(node) : definition.attributeType();
        Object converted = convert(node, type);
        return switch (type) {
            case STRING -> new AVString(name, (String) converted);
            case DECIMAL -> new AVDecimal(name, (BigDecimal) converted);
            case BOOLEAN -> new AVBoolean(name, (Boolean) converted);
        };
    }

    private Object convert(JsonNode node, AttributeType type) {
        if (node == null || node.isNull()) {
            return null;
        }
        return switch (type) {
            case STRING -> objectMapper.convertValue(node, String.class);
            case DECIMAL -> objectMapper.convertValue(node, BigDecimal.class);
            case BOOLEAN -> objectMapper.convertValue(node, Boolean.class);
        };
    }

    private AttributeType inferType(JsonNode node) {
        if (node == null) {
            return AttributeType.STRING;
        }
        if (node.isBoolean()) {
            return AttributeType.BOOLEAN;
        }
        if (node.isNumber()) {
            return AttributeType.DECIMAL;
        }
        return AttributeType.STRING;
    }
}
