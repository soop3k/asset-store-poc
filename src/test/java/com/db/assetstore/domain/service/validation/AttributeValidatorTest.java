package com.db.assetstore.domain.service.validation;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.model.type.AttributeType;
import com.db.assetstore.domain.service.type.AttributeDefinition;
import com.db.assetstore.domain.service.type.ConstraintDefinition;
import com.db.assetstore.domain.service.type.ConstraintDefinition.Rule;
import com.db.assetstore.domain.service.validation.rule.CustomValidationRuleRegistry;
import com.db.assetstore.domain.service.validation.ValidationMode;
import com.db.assetstore.domain.service.validation.rule.RuleViolationException;
import com.db.assetstore.domain.service.validation.rule.ValidationRuleFactory;
import com.db.assetstore.testutil.TestAttributeDefinitionRegistry;
import com.db.assetstore.testutil.validation.MatchingAttributesRule;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static com.db.assetstore.testutil.AttributeTestHelpers.constraint;
import static com.db.assetstore.testutil.AttributeTestHelpers.definition;

class AttributeValidatorTest {

    @Test
    void throwsWhenRequiredAttributeMissing() {
        var nameDefinition = definition(AssetType.CRE, "name", AttributeType.STRING);
        ConstraintDefinition type = constraint(nameDefinition, Rule.TYPE);
        ConstraintDefinition required = constraint(nameDefinition, Rule.REQUIRED);
        Map<String, AttributeDefinition> defs = Map.of("name", nameDefinition);
        Map<String, List<ConstraintDefinition>> constraints = Map.of("name", List.of(type, required));
        AttributeValidator validator = validator(defs, constraints);

        assertThatThrownBy(() -> validator.validate(AssetType.CRE, AttributesCollection.empty(), ValidationMode.FULL))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining("required");
    }

    @Test
    void patchSkipsRequiredWhenAttributeOmitted() {
        var nameDefinition = definition(AssetType.CRE, "name", AttributeType.STRING);
        ConstraintDefinition type = constraint(nameDefinition, Rule.TYPE);
        ConstraintDefinition required = constraint(nameDefinition, Rule.REQUIRED);
        Map<String, AttributeDefinition> defs = Map.of("name", nameDefinition);
        Map<String, List<ConstraintDefinition>> constraints = Map.of("name", List.of(type, required));
        AttributeValidator validator = validator(defs, constraints);

        assertThatCode(() -> validator.validate(AssetType.CRE, AttributesCollection.empty(), ValidationMode.PARTIAL))
                .doesNotThrowAnyException();
    }

    @Test
    void patchStillFailsWhenRequiredAttributeProvidedWithoutValue() {
        var nameDefinition = definition(AssetType.CRE, "name", AttributeType.STRING);
        ConstraintDefinition type = constraint(nameDefinition, Rule.TYPE);
        ConstraintDefinition required = constraint(nameDefinition, Rule.REQUIRED);
        Map<String, AttributeDefinition> defs = Map.of("name", nameDefinition);
        Map<String, List<ConstraintDefinition>> constraints = Map.of("name", List.of(type, required));
        AttributeValidator validator = validator(defs, constraints);

        assertThatThrownBy(() -> validator.validate(AssetType.CRE,
                AttributesCollection.fromFlat(List.of(new AVString("name", null)))))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining("required");
    }

    @Test
    void strictModeRejectsUnknownAttributes() {
        var nameDefinition = definition(AssetType.CRE, "name", AttributeType.STRING);
        ConstraintDefinition type = constraint(nameDefinition, Rule.TYPE);
        Map<String, AttributeDefinition> defs = Map.of("name", nameDefinition);
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
        var name = definition(AssetType.CRE, "name", AttributeType.STRING);
        var code = definition(AssetType.CRE, "code", AttributeType.STRING);
        ConstraintDefinition typeName = constraint(name, Rule.TYPE);
        ConstraintDefinition typeCode = constraint(code, Rule.TYPE);
        ConstraintDefinition custom = constraint(name, Rule.CUSTOM, "matchingAttributes");
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
        var area = definition(AssetType.CRE, "area", AttributeType.DECIMAL);
        ConstraintDefinition type = constraint(area, Rule.TYPE);
        ConstraintDefinition range = constraint(area, Rule.MIN_MAX, "10,20");
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
        var status = definition(AssetType.CRE, "status", AttributeType.STRING);
        ConstraintDefinition type = constraint(status, Rule.TYPE);
        ConstraintDefinition allowed = constraint(status, Rule.ENUM, "draft,active,archived");
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
        var description = definition(AssetType.CRE, "description", AttributeType.STRING);
        ConstraintDefinition type = constraint(description, Rule.TYPE);
        ConstraintDefinition length = constraint(description, Rule.LENGTH, "5");
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
        var weight = definition(AssetType.CRE, "weight", AttributeType.DECIMAL);
        ConstraintDefinition type = constraint(weight, Rule.TYPE);
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
        var registry = TestAttributeDefinitionRegistry.builder()
                .withAttributes(AssetType.CRE, defs, constraints)
                .build();
        return new AttributeValidator(registry, factory);
    }
}
