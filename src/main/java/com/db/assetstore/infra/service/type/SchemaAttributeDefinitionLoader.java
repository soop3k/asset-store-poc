package com.db.assetstore.infra.service.type;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.type.AttributeType;
import com.db.assetstore.domain.service.type.AttributeDefinition;
import com.db.assetstore.domain.service.type.AttributeDefinitionLoader;
import com.db.assetstore.domain.service.type.ConstraintDefinition;
import com.db.assetstore.domain.service.type.TypeSchemaRegistry;
import com.db.assetstore.util.CollectionUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class SchemaAttributeDefinitionLoader implements AttributeDefinitionLoader {

    private final TypeSchemaRegistry typeSchemaRegistry;

    @Override
    public AttributeDefinitions load(AssetType assetType) {
        return typeSchemaRegistry.getSchemaNode(assetType)
                .map(node -> parseSchema(assetType, node))
                .orElse(AttributeDefinitions.empty());
    }

    private AttributeDefinitions parseSchema(AssetType assetType, JsonNode schema) {
        Map<String, AttributeDefinition> definitions = new LinkedHashMap<>();
        Map<String, List<ConstraintDefinition>> constraints = new LinkedHashMap<>();
        if (schema == null || !schema.isObject()) {
            log.debug("Schema for {} is not an object node", assetType);
            return new AttributeDefinitions(definitions, constraints);
        }

        Set<String> required = readRequired(schema.get("required"));
        JsonNode properties = schema.get("properties");
        if (properties == null || !properties.isObject()) {
            return new AttributeDefinitions(definitions, constraints);
        }

        Iterator<String> fieldNames = properties.fieldNames();
        while (fieldNames.hasNext()) {
            String name = fieldNames.next();
            JsonNode definitionNode = properties.get(name);
            AttributeType attributeType = readAttributeType(definitionNode);
            var attributeDefinition = new AttributeDefinition(assetType, name, attributeType);
            definitions.put(name, attributeDefinition);

            var attributeConstraints = new ArrayList<ConstraintDefinition>();
            attributeConstraints.add(new ConstraintDefinition(attributeDefinition, ConstraintDefinition.Rule.TYPE, null));
            if (required.contains(name)) {
                attributeConstraints.add(new ConstraintDefinition(attributeDefinition, ConstraintDefinition.Rule.REQUIRED, null));
            }
            readMinMaxRule(definitionNode)
                    .map(value -> new ConstraintDefinition(attributeDefinition, ConstraintDefinition.Rule.MIN_MAX, value))
                    .ifPresent(attributeConstraints::add);
            readEnumRule(definitionNode)
                    .map(value -> new ConstraintDefinition(attributeDefinition, ConstraintDefinition.Rule.ENUM, value))
                    .ifPresent(attributeConstraints::add);
            readLengthRule(definitionNode)
                    .map(value -> new ConstraintDefinition(attributeDefinition, ConstraintDefinition.Rule.LENGTH, value))
                    .ifPresent(attributeConstraints::add);

            constraints.put(name, List.copyOf(attributeConstraints));
        }

        return new AttributeDefinitions(definitions, constraints);
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

    private static Optional<String> readMinMaxRule(JsonNode definitionNode) {
        if (definitionNode == null || !definitionNode.isObject()) {
            return Optional.empty();
        }
        JsonNode min = definitionNode.get("minimum");
        JsonNode max = definitionNode.get("maximum");
        if (min == null && max == null) {
            return Optional.empty();
        }
        String minValue = null;
        String maxValue = null;
        if (min != null && min.isNumber()) {
            minValue = min.asText();
        }
        if (max != null && max.isNumber()) {
            maxValue = max.asText();
        }
        if (minValue == null && maxValue == null) {
            return Optional.empty();
        }
        StringBuilder builder = new StringBuilder();
        builder.append(minValue != null ? minValue : "");
        builder.append(',');
        builder.append(maxValue != null ? maxValue : "");
        return Optional.of(builder.toString());
    }

    private static Optional<String> readEnumRule(JsonNode definitionNode) {
        if (definitionNode == null || !definitionNode.isObject()) {
            return Optional.empty();
        }
        JsonNode enumNode = definitionNode.get("enum");
        if (enumNode == null || !enumNode.isArray() || enumNode.isEmpty()) {
            return Optional.empty();
        }
        List<String> values = new ArrayList<>();
        enumNode.forEach(node -> {
            if (node.isValueNode()) {
                values.add(node.asText());
            }
        });
        var safeValues = CollectionUtils.<List<String>>emptyIfNullOrEmpty(values);
        if (safeValues.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(String.join(",", safeValues));
    }

    private static Optional<String> readLengthRule(JsonNode definitionNode) {
        if (definitionNode == null || !definitionNode.isObject()) {
            return Optional.empty();
        }
        JsonNode maxLength = definitionNode.get("maxLength");
        if (maxLength == null || !maxLength.isNumber()) {
            return Optional.empty();
        }
        return Optional.of(maxLength.asText());
    }
}
