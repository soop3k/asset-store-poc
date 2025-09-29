package com.db.assetstore.infra.service.type;

import com.db.assetstore.domain.model.asset.AssetType;
import com.db.assetstore.domain.model.type.AttributeType;
import com.db.assetstore.domain.service.type.AttributeDefinition;
import com.db.assetstore.domain.service.type.AttributeDefinitionLoader;
import com.db.assetstore.domain.service.type.ConstraintDefinition;
import com.db.assetstore.domain.service.type.TypeSchemaRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


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
        var definitions = new LinkedHashMap<String, AttributeDefinition>();
        var constraints = new LinkedHashMap<String, List<ConstraintDefinition>>();

        var required = readRequired(schema.get("required"));
        var properties = schema.get("properties");
        if (properties == null || !properties.isObject()) {
            return new AttributeDefinitions(definitions, constraints);
        }

        for (var entry : properties.properties()) {
            var name = entry.getKey();
            var definitionNode = entry.getValue();
            var attributeType = readAttributeType(definitionNode);
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
            readCustomRules(attributeDefinition, definitionNode)
                    .forEach(attributeConstraints::add);

            constraints.put(name, List.copyOf(attributeConstraints));
        }

        return new AttributeDefinitions(definitions, constraints);
    }

    private static Set<String> readRequired(JsonNode requiredNode) {
        if (requiredNode == null || !requiredNode.isArray()) {
            return Collections.emptySet();
        }

        return requiredNode.valueStream()
                .filter(JsonNode::isTextual)
                .map(JsonNode::asText)
                .collect(Collectors.toSet());
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
            case "date" -> AttributeType.DATE;
            default -> AttributeType.STRING;
        };
    }

    private static Optional<String> readMinMaxRule(JsonNode definitionNode) {
        if (definitionNode == null || !definitionNode.isObject()) {
            return Optional.empty();
        }
        var min = definitionNode.get("minimum");
        var max = definitionNode.get("maximum");
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
        String builder = (minValue != null ? minValue : "") +  ',' + (maxValue != null ? maxValue : "");
        return Optional.of(builder);
    }

    private static Optional<String> readEnumRule(JsonNode definitionNode) {
        if (definitionNode == null || !definitionNode.isObject()) {
            return Optional.empty();
        }
        var enumNode = definitionNode.get("enum");
        if (enumNode == null || !enumNode.isArray() || enumNode.isEmpty()) {
            return Optional.empty();
        }

        var values = enumNode.valueStream()
                .filter(JsonNode::isValueNode)
                .map(JsonNode::asText)
                .toList();

        if (values.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(String.join(",", values));
    }

    private static Optional<String> readLengthRule(JsonNode definitionNode) {
        if (definitionNode == null || !definitionNode.isObject()) {
            return Optional.empty();
        }
        var maxLength = definitionNode.get("maxLength");
        if (maxLength == null || !maxLength.isNumber()) {
            return Optional.empty();
        }
        return Optional.of(maxLength.asText());
    }

    private static List<ConstraintDefinition> readCustomRules(AttributeDefinition definition, JsonNode definitionNode) {
        if (definitionNode == null || !definitionNode.isObject()) {
            return List.of();
        }

        var customNode = definitionNode.get("x-customRules");
        if (customNode == null || customNode.isNull()) {
            return List.of();
        }

        var collected = new ArrayList<ConstraintDefinition>();
        if (customNode.isArray()) {
            customNode.forEach(node -> appendCustomRule(definition, node, collected));
        } else {
            appendCustomRule(definition, customNode, collected);
        }
        return collected;
    }

    private static void appendCustomRule(AttributeDefinition definition,
                                         JsonNode node,
                                         List<ConstraintDefinition> target) {
        if (!node.isTextual()) {
            return;
        }

        var val = node.asText();
        if (val != null && !val.isBlank()) {
            target.add(new ConstraintDefinition(definition, ConstraintDefinition.Rule.CUSTOM, val));
        }
    }

}
