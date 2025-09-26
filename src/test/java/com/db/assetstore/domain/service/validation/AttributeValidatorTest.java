package com.db.assetstore.domain.service.validation;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.model.type.AttributeType;
import com.db.assetstore.domain.service.type.AttributeDefinition;
import com.db.assetstore.domain.service.type.AttributeDefinitionRegistry;
import com.db.assetstore.domain.service.type.ConstraintDefinition;
import com.db.assetstore.domain.service.type.ConstraintDefinition.Rule;
import com.db.assetstore.domain.service.validation.custom.MatchingAttributesRule;
import com.db.assetstore.domain.service.validation.rule.CustomValidationRuleRegistry;
import com.db.assetstore.domain.service.validation.ValidationMode;
import com.db.assetstore.domain.service.validation.rule.RuleViolationException;
import com.db.assetstore.domain.service.validation.rule.ValidationRuleFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttributeValidatorTest {

    @Test
    void throwsWhenRequiredAttributeMissing() {
        var definition = new AttributeDefinition(AssetType.CRE, "name", AttributeType.STRING, true);
        ConstraintDefinition type = new ConstraintDefinition(definition, Rule.TYPE, null);
        ConstraintDefinition required = new ConstraintDefinition(definition, Rule.REQUIRED, null);
        Map<String, AttributeDefinition> defs = Map.of("name", definition);
        Map<String, List<ConstraintDefinition>> constraints = Map.of("name", List.of(type, required));
        AttributeValidator validator = validator(defs, constraints);

        assertThatThrownBy(() -> validator.validate(AssetType.CRE, AttributesCollection.empty(), ValidationMode.FULL))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining("required");
    }

    @Test
    void patchSkipsRequiredWhenAttributeOmitted() {
        var definition = new AttributeDefinition(AssetType.CRE, "name", AttributeType.STRING, true);
        ConstraintDefinition type = new ConstraintDefinition(definition, Rule.TYPE, null);
        ConstraintDefinition required = new ConstraintDefinition(definition, Rule.REQUIRED, null);
        Map<String, AttributeDefinition> defs = Map.of("name", definition);
        Map<String, List<ConstraintDefinition>> constraints = Map.of("name", List.of(type, required));
        AttributeValidator validator = validator(defs, constraints);

        assertThatCode(() -> validator.validate(AssetType.CRE, AttributesCollection.empty(), ValidationMode.PARTIAL))
                .doesNotThrowAnyException();
    }

    @Test
    void patchStillFailsWhenRequiredAttributeProvidedWithoutValue() {
        var definition = new AttributeDefinition(AssetType.CRE, "name", AttributeType.STRING, true);
        ConstraintDefinition type = new ConstraintDefinition(definition, Rule.TYPE, null);
        ConstraintDefinition required = new ConstraintDefinition(definition, Rule.REQUIRED, null);
        Map<String, AttributeDefinition> defs = Map.of("name", definition);
        Map<String, List<ConstraintDefinition>> constraints = Map.of("name", List.of(type, required));
        AttributeValidator validator = validator(defs, constraints);

        assertThatThrownBy(() -> validator.validate(AssetType.CRE,
                AttributesCollection.fromFlat(List.of(new AVString("name", null)))))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining("required");
    }

    @Test
    void strictModeRejectsUnknownAttributes() {
        var definition = new AttributeDefinition(AssetType.CRE, "name", AttributeType.STRING, false);
        ConstraintDefinition type = new ConstraintDefinition(definition, Rule.TYPE, null);
        Map<String, AttributeDefinition> defs = Map.of("name", definition);
        Map<String, List<ConstraintDefinition>> constraints = Map.of("name", List.of(type));
        AttributeValidator validator = validator(defs, constraints);

        assertThatThrownBy(() -> validator.validate(AssetType.CRE,
                AttributesCollection.fromFlat(List.of(new AVString("unknown", "value"))),
                ValidationMode.STRICT))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining("Unknown attribute definition");
    }

    @Test
    void customRuleValidatesDependentAttributes() {
        var name = new AttributeDefinition(AssetType.CRE, "name", AttributeType.STRING, false);
        var code = new AttributeDefinition(AssetType.CRE, "code", AttributeType.STRING, false);
        ConstraintDefinition typeName = new ConstraintDefinition(name, Rule.TYPE, null);
        ConstraintDefinition typeCode = new ConstraintDefinition(code, Rule.TYPE, null);
        ConstraintDefinition custom = new ConstraintDefinition(name, Rule.CUSTOM,
                "matchingAttributes");
        Map<String, AttributeDefinition> defs = Map.of("name", name, "code", code);
        Map<String, List<ConstraintDefinition>> constraints = Map.of(
                "name", List.of(typeName, custom),
                "code", List.of(typeCode)
        );
        AttributeValidator validator = validator(defs, constraints);

        validator.validate(AssetType.CRE,
                AttributesCollection.fromFlat(List.of(new AVString("name", "alpha"), new AVString("code", "alpha"))));

        assertThatThrownBy(() -> validator.validate(AssetType.CRE,
                AttributesCollection.fromFlat(List.of(new AVString("name", "alpha"), new AVString("code", "beta")))))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining("Attributes must match");
    }

    @Test
    void validatesValuesWithinNumericBounds() {
        var area = new AttributeDefinition(AssetType.CRE, "area", AttributeType.DECIMAL, false);
        ConstraintDefinition type = new ConstraintDefinition(area, Rule.TYPE, null);
        ConstraintDefinition range = new ConstraintDefinition(area, Rule.MIN_MAX, "10,20");
        Map<String, AttributeDefinition> defs = Map.of("area", area);
        Map<String, List<ConstraintDefinition>> constraints = Map.of("area", List.of(type, range));
        AttributeValidator validator = validator(defs, constraints);

        assertThatCode(() -> validator.validate(AssetType.CRE,
                AttributesCollection.fromFlat(List.of(AVDecimal.of("area", 15)))))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> validator.validate(AssetType.CRE,
                AttributesCollection.fromFlat(List.of(AVDecimal.of("area", 25)))))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining("exceeds maximum");

        assertThatThrownBy(() -> validator.validate(AssetType.CRE,
                AttributesCollection.fromFlat(List.of(AVDecimal.of("area", 5)))))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining("less than minimum");
    }

    @Test
    void rejectsValuesOutsideEnumList() {
        var status = new AttributeDefinition(AssetType.CRE, "status", AttributeType.STRING, false);
        ConstraintDefinition type = new ConstraintDefinition(status, Rule.TYPE, null);
        ConstraintDefinition allowed = new ConstraintDefinition(status, Rule.ENUM, "draft,active,archived");
        Map<String, AttributeDefinition> defs = Map.of("status", status);
        Map<String, List<ConstraintDefinition>> constraints = Map.of("status", List.of(type, allowed));
        AttributeValidator validator = validator(defs, constraints);

        assertThatCode(() -> validator.validate(AssetType.CRE,
                AttributesCollection.fromFlat(List.of(new AVString("status", "active")))))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> validator.validate(AssetType.CRE,
                AttributesCollection.fromFlat(List.of(new AVString("status", "pending")))))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining("is not allowed");
    }

    @Test
    void rejectsValuesExceedingConfiguredLength() {
        var description = new AttributeDefinition(AssetType.CRE, "description", AttributeType.STRING, false);
        ConstraintDefinition type = new ConstraintDefinition(description, Rule.TYPE, null);
        ConstraintDefinition length = new ConstraintDefinition(description, Rule.LENGTH, "5");
        Map<String, AttributeDefinition> defs = Map.of("description", description);
        Map<String, List<ConstraintDefinition>> constraints = Map.of("description", List.of(type, length));
        AttributeValidator validator = validator(defs, constraints);

        assertThatCode(() -> validator.validate(AssetType.CRE,
                AttributesCollection.fromFlat(List.of(new AVString("description", "short")))))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> validator.validate(AssetType.CRE,
                AttributesCollection.fromFlat(List.of(new AVString("description", "too long")))))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining("Length must be less than or equal to 5");
    }

    @Test
    void rejectsAttributesWithMismatchedType() {
        var weight = new AttributeDefinition(AssetType.CRE, "weight", AttributeType.DECIMAL, false);
        ConstraintDefinition type = new ConstraintDefinition(weight, Rule.TYPE, null);
        Map<String, AttributeDefinition> defs = Map.of("weight", weight);
        Map<String, List<ConstraintDefinition>> constraints = Map.of("weight", List.of(type));
        AttributeValidator validator = validator(defs, constraints);

        assertThatThrownBy(() -> validator.validate(AssetType.CRE,
                AttributesCollection.fromFlat(List.of(new AVString("weight", "heavy")))))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining("Attribute type mismatch");
    }

    private static CustomValidationRuleRegistry customRegistry() {
        return new CustomValidationRuleRegistry(List.of(new MatchingAttributesRule()));
    }

    private static AttributeValidator validator(Map<String, AttributeDefinition> defs,
                                                Map<String, List<ConstraintDefinition>> constraints) {
        var customRegistry = customRegistry();
        var factory = new ValidationRuleFactory(customRegistry);
        return new AttributeValidator(new FixedRegistry(defs, constraints), factory);
    }

    private static final class FixedRegistry implements AttributeDefinitionRegistry {

        private final Map<String, AttributeDefinition> definitions;
        private final Map<String, List<ConstraintDefinition>> constraints;

        private FixedRegistry(Map<String, AttributeDefinition> definitions,
                              Map<String, List<ConstraintDefinition>> constraints) {
            this.definitions = definitions;
            this.constraints = constraints;
        }

        @Override
        public Map<String, AttributeDefinition> getDefinitions(AssetType type) {
            return definitions;
        }

        @Override
        public Map<String, List<ConstraintDefinition>> getConstraints(AssetType type) {
            return constraints;
        }

        @Override
        public void refresh() {
            // no-op
        }
    }
}
