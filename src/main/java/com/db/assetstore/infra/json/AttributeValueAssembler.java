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
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class AttributeValueAssembler {

    private final AttributeDefinitionRegistry attributeDefinitionRegistry;

    AttributeValueAssembler(AttributeDefinitionRegistry attributeDefinitionRegistry) {
        this.attributeDefinitionRegistry = attributeDefinitionRegistry;
    }

    AttributesCollection assemble(AssetType type, List<ParsedAttributeValue> rawValues) {
        if (rawValues.isEmpty()) {
            return AttributesCollection.empty();
        }
        var definitions = attributeDefinitionRegistry.getDefinitions(type);
        var values = new ArrayList<AttributeValue<?>>();

        for (var raw : rawValues) {
            var definition = definitions.get(raw.name());
            if (definition == null) {
                throw AttributeParsingException.missingDefinition(raw.name());
            }
            values.add(createValue(raw, definition));
        }

        return AttributesCollection.fromFlat(values);
    }

    private AttributeValue<?> createValue(ParsedAttributeValue raw, AttributeDefinition definition) {
        var type = definition.attributeType();
        var converted = convert(raw, type);
        return switch (type) {
            case STRING -> new AVString(raw.name(), (String) converted);
            case DECIMAL -> new AVDecimal(raw.name(), (BigDecimal) converted);
            case BOOLEAN -> new AVBoolean(raw.name(), (Boolean) converted);
        };
    }

    private Object convert(ParsedAttributeValue raw, AttributeType expectedType) {
        JsonNode node = raw.node();
        if (node == null || node.isNull()) {
            return null;
        }

        return switch (expectedType) {
            case STRING -> parseString(raw.name(), node);
            case DECIMAL -> parseDecimal(raw.name(), node);
            case BOOLEAN -> parseBoolean(raw.name(), node);
        };
    }

    private String parseString(String name, JsonNode node) {
        if (node.isTextual()) {
            return node.textValue();
        }
        if (node.isNumber() || node.isBoolean()) {
            return node.asText();
        }
        throw AttributeParsingException.incompatibleType(name, AttributeType.STRING, node.getNodeType().name());
    }

    private BigDecimal parseDecimal(String name, JsonNode node) {
        if (node.isNumber()) {
            return node.decimalValue();
        }
        if (node.isTextual()) {
            try {
                return new BigDecimal(node.textValue());
            } catch (NumberFormatException ex) {
                throw AttributeParsingException.incompatibleType(name, AttributeType.DECIMAL, node.toString());
            }
        }
        throw AttributeParsingException.incompatibleType(name, AttributeType.DECIMAL, node.getNodeType().name());
    }

    private Boolean parseBoolean(String name, JsonNode node) {
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        if (node.isTextual()) {
            var text = node.textValue();
            if ("true".equalsIgnoreCase(text)) {
                return Boolean.TRUE;
            }
            if ("false".equalsIgnoreCase(text)) {
                return Boolean.FALSE;
            }
            throw AttributeParsingException.incompatibleType(name, AttributeType.BOOLEAN, node.toString());
        }
        throw AttributeParsingException.incompatibleType(name, AttributeType.BOOLEAN, node.getNodeType().name());
    }
}
