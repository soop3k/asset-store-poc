package com.db.assetstore.domain.service.validation;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.attribute.AttributesCollection;
import com.db.assetstore.domain.model.type.AVDate;
import com.db.assetstore.domain.model.type.AVDecimal;
import com.db.assetstore.domain.model.type.AVString;
import com.db.assetstore.domain.model.type.AttributeType;
import com.db.assetstore.domain.service.type.AttributeDefinition;
import com.db.assetstore.domain.service.type.ConstraintDefinition;
import com.db.assetstore.domain.service.type.ConstraintDefinition.Rule;
import com.db.assetstore.domain.service.validation.rule.AttributeValidationErrorsException;
import com.db.assetstore.domain.service.validation.ValidationMode;
import com.db.assetstore.domain.service.validation.rule.CustomValidationRuleRegistry;
import com.db.assetstore.domain.service.validation.rule.ValidationRuleFactory;
import com.db.assetstore.testutil.InMemoryAttributeDefinitionLoader;
import com.db.assetstore.testutil.validation.MatchingAttributesRule;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
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
                .isInstanceOf(AttributeValidationErrorsException.class)
                .satisfies(ex -> {
                    var errors = (AttributeValidationErrorsException) ex;
                    assertThat(errors.violations()).anySatisfy(v -> {
                        assertThat(v.rule()).isEqualTo(Rule.REQUIRED.name());
                        assertThat(v.attributes()).contains("name");
                        assertThat(v.expected()).isEqualTo("non-null value");
                        assertThat(v.actual()).isEqualTo("<absent>");
                    });
                });
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
                .isInstanceOf(AttributeValidationErrorsException.class)
                .satisfies(ex -> {
                    var errors = (AttributeValidationErrorsException) ex;
                    assertThat(errors.violations()).anySatisfy(v -> {
                        assertThat(v.rule()).isEqualTo(Rule.REQUIRED.name());
                        assertThat(v.actual()).isNull();
                    });
                });
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
                .isInstanceOf(AttributeValidationErrorsException.class)
                .satisfies(ex -> {
                    var errors = (AttributeValidationErrorsException) ex;
                    assertThat(errors.violations()).anySatisfy(v -> {
                        assertThat(v.rule()).isEqualTo("UNKNOWN_ATTRIBUTE");
                        assertThat(v.attributes()).contains("unknown");
                        assertThat(v.expected()).asString().contains("name");
                    });
                });
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
                .isInstanceOf(AttributeValidationErrorsException.class)
                .satisfies(ex -> {
                    var errors = (AttributeValidationErrorsException) ex;
                    assertThat(errors.violations()).anySatisfy(v -> {
                        assertThat(v.rule()).isEqualTo(Rule.CUSTOM.name());
                        assertThat(v.message()).contains("Attributes must match");
                    });
                });
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
                .isInstanceOf(AttributeValidationErrorsException.class)
                .satisfies(ex -> assertNumericViolation((AttributeValidationErrorsException) ex,
                        Rule.MIN_MAX.name(), "<=" + new java.math.BigDecimal("20"), new java.math.BigDecimal("25")));

        assertThatThrownBy(() -> validator.validate(AssetType.CRE,
                AttributesCollection.fromFlat(List.of(AVDecimal.of("area", 5)))))
                .isInstanceOf(AttributeValidationErrorsException.class)
                .satisfies(ex -> assertNumericViolation((AttributeValidationErrorsException) ex,
                        Rule.MIN_MAX.name(), ">=" + new java.math.BigDecimal("10"), new java.math.BigDecimal("5")));
    }

    @Test
    void validatesValuesWithinDateBounds() {
        var area = definition(AssetType.CRE, "constructionDate", AttributeType.DATE);
        ConstraintDefinition type = constraint(area, Rule.TYPE);
        ConstraintDefinition range = constraint(area, Rule.MIN_MAX, "2025-09-01T00:00:00Z, 2025-10-01T00:00:00Z");
        Map<String, AttributeDefinition> defs = Map.of("constructionDate", area);
        Map<String, List<ConstraintDefinition>> constraints = Map.of("constructionDate", List.of(type, range));
        AttributeValidator validator = validator(defs, constraints);

        assertThatCode(() -> validator.validate(AssetType.CRE,
                AttributesCollection.fromFlat(List.of(AVDate.of("constructionDate", Instant.parse("2025-09-02T00:00:00Z"))))))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> validator.validate(AssetType.CRE,
                AttributesCollection.fromFlat(List.of(AVDate.of("constructionDate", Instant.parse("2025-12-02T00:00:00Z"))))))
                .isInstanceOf(AttributeValidationErrorsException.class);

        assertThatThrownBy(() -> validator.validate(AssetType.CRE,
                AttributesCollection.fromFlat(List.of(AVDecimal.of("constructionDate", 5)))))
                .isInstanceOf(AttributeValidationErrorsException.class);

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
                .isInstanceOf(AttributeValidationErrorsException.class)
                .satisfies(ex -> {
                    var errors = (AttributeValidationErrorsException) ex;
                    assertThat(errors.violations()).anySatisfy(v -> {
                        assertThat(v.rule()).isEqualTo(Rule.ENUM.name());
                        assertThat(v.expected().toString()).contains("draft");
                        assertThat(v.actual()).isEqualTo("pending");
                    });
                });
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
                .isInstanceOf(AttributeValidationErrorsException.class)
                .satisfies(ex -> {
                    var errors = (AttributeValidationErrorsException) ex;
                    assertThat(errors.violations()).anySatisfy(v -> {
                        assertThat(v.rule()).isEqualTo(Rule.LENGTH.name());
                    });
                });
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
                .isInstanceOf(AttributeValidationErrorsException.class)
                .satisfies(ex -> {
                    var errors = (AttributeValidationErrorsException) ex;
                    assertThat(errors.violations()).anySatisfy(v -> {
                        assertThat(v.rule()).isEqualTo(Rule.TYPE.name());
                        assertThat(v.expected()).isEqualTo(AttributeType.DECIMAL);
                        assertThat(v.actual()).isEqualTo(AttributeType.STRING);
                    });
                });
    }

    private void assertNumericViolation(AttributeValidationErrorsException exception,
                                        String rule,
                                        Object expected,
                                        Object actual) {
        assertThat(exception.violations()).anySatisfy(v -> {
            assertThat(v.rule()).isEqualTo(rule);
            assertThat(v.expected()).isEqualTo(expected);
            assertThat(v.actual()).isEqualTo(actual);
        });
    }

    private static CustomValidationRuleRegistry customRegistry() {
        return new CustomValidationRuleRegistry(List.of(new MatchingAttributesRule()));
    }

    private static AttributeValidator validator(Map<String, AttributeDefinition> defs,
                                                Map<String, List<ConstraintDefinition>> constraints) {
        var customRegistry = customRegistry();
        var factory = new ValidationRuleFactory(customRegistry);
        var registry = InMemoryAttributeDefinitionLoader.builder()
                .withAttributes(AssetType.CRE, defs, constraints)
                .buildRegistry();
        return new AttributeValidator(registry, factory);
    }
}
