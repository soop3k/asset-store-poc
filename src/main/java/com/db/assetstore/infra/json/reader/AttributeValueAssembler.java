package com.db.assetstore.infra.json.reader;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.attribute.AttributeValue;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.domain.model.type.*;
import com.db.assetstore.domain.service.type.AttributeDefinition;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

@Component
public class AttributeValueAssembler {

    private final AttributeDefinitionRegistry attributeDefinitionRegistry;

    public AttributeValueAssembler(AttributeDefinitionRegistry attributeDefinitionRegistry) {
        this.attributeDefinitionRegistry = attributeDefinitionRegistry;
    }

    AttributesCollection assemble(AssetType type, List<ParsedAttributeValue> rawValues) {
        var definitions = attributeDefinitionRegistry.getDefinitions(type);
        var values = new ArrayList<AttributeValue<?>>();

        for (var raw : rawValues) {
            var definition = definitions.get(raw.name());
            if (definition == null) {
                throw AttributeParsingException.missingDefinition(raw.name());
            }
            Optional.ofNullable(createValue(raw, definition)).ifPresent(values::add);
        }

        return AttributesCollection.fromFlat(values);
    }

    private AttributeValue<?> createValue(ParsedAttributeValue raw, AttributeDefinition definition) {
        var type = definition.attributeType();
        var node = raw.node();
        var name = raw.name();

        return switch (type) {
            case STRING -> AVString.of(name, valueFromNode(name, node, this::parseString));
            case DECIMAL -> AVDecimal.of(name, valueFromNode(name, node, this::parseDecimal));
            case BOOLEAN -> AVBoolean.of(name, valueFromNode(name, node, this::parseBoolean));
            case DATE -> AVDate.of(name, valueFromNode(name, node, this::parseDate));
        };
    }

    private <R> R valueFromNode(String name, JsonNode node, BiFunction<String, JsonNode, R> parser) {
        if (node == null || node.isNull()) {
            return null;
        }

        return parser.apply(name, node);
    }

    private Instant parseDate(String name, JsonNode node) {
        try {
            return Instant.parse(node.textValue());
        } catch(DateTimeParseException ex){
            throw AttributeParsingException.invalidValue(name, node.textValue(), AttributeType.DATE);
        }
    }

    private String parseString(String name, JsonNode node) {
        if (node.isTextual() || node.isNumber() || node.isBoolean()) {
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
                throw AttributeParsingException.invalidValue(name, node.asText(""), AttributeType.DECIMAL);
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
            throw AttributeParsingException.invalidValue(name, node.asText(""), AttributeType.BOOLEAN);
        }
        throw AttributeParsingException.incompatibleType(name, AttributeType.BOOLEAN, node.getNodeType().name());
    }
}
