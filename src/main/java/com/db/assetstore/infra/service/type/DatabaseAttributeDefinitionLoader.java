package com.db.assetstore.infra.service.type;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.service.type.AttributeDefinition;
import com.db.assetstore.domain.service.type.AttributeDefinitionLoader;
import com.db.assetstore.domain.service.type.ConstraintDefinition;
import com.db.assetstore.domain.service.validation.rule.CustomRuleNameResolver;
import com.db.assetstore.infra.mapper.AttributeDefinitionMapper;
import com.db.assetstore.infra.repository.AttributeDefRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Order(1)
public class DatabaseAttributeDefinitionLoader implements AttributeDefinitionLoader {

    private final AttributeDefRepository attributeDefRepository;
    private final AttributeDefinitionMapper attributeDefinitionMapper;
    private final ObjectMapper objectMapper;

    @Override
    public AttributeDefinitions load(AssetType type) {
        var definitions = new LinkedHashMap<String, AttributeDefinition>();
        var constraints = new LinkedHashMap<String, List<ConstraintDefinition>>();

        var entities = attributeDefRepository.findAllByTypeWithConstraints(type);
        for (var entity : entities) {
            var attributeDefinition = attributeDefinitionMapper.toDomain(entity);
            definitions.put(entity.getName(), attributeDefinition);

            var attributeConstraints = new ArrayList<ConstraintDefinition>();
            attributeConstraints.add(new ConstraintDefinition(attributeDefinition, ConstraintDefinition.Rule.TYPE, null));
            if (entity.isRequired()) {
                attributeConstraints.add(new ConstraintDefinition(attributeDefinition, ConstraintDefinition.Rule.REQUIRED, null));
            }
            attributeConstraints.addAll(
                    attributeDefinitionMapper.toDomainConstraints(attributeDefinition, entity.getConstraints())
                            .stream()
                            .map(this::normalizeCustomConstraint)
                            .toList());
            constraints.put(entity.getName(), List.copyOf(attributeConstraints));
        }

        return new AttributeDefinitions(definitions, constraints);
    }

    private ConstraintDefinition normalizeCustomConstraint(ConstraintDefinition constraint) {
        if (constraint.rule() != ConstraintDefinition.Rule.CUSTOM) {
            return constraint;
        }
        String name = extractCustomRuleName(constraint.value());
        return new ConstraintDefinition(constraint.attribute(), constraint.rule(), name);
    }

    private String extractCustomRuleName(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        var trimmed = rawValue.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.startsWith("{")) {
            try {
                JsonNode node = objectMapper.readTree(trimmed);
                if (node.hasNonNull("name")) {
                    return node.get("name").asText();
                }
                if (node.hasNonNull("class")) {
                    return CustomRuleNameResolver.fromClassName(node.get("class").asText());
                }
            } catch (JsonProcessingException ex) {
                throw new IllegalStateException("Unable to parse custom constraint value: " + rawValue, ex);
            }
        }
        return trimmed;
    }
}
