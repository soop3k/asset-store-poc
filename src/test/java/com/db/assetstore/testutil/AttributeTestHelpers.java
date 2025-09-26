package com.db.assetstore.testutil;

import com.db.assetstore.AssetType;
import com.db.assetstore.domain.model.type.AttributeType;
import com.db.assetstore.domain.service.type.AttributeDefinition;
import com.db.assetstore.domain.service.type.ConstraintDefinition;

import java.util.List;

public final class AttributeTestHelpers {

    private AttributeTestHelpers() {
    }

    public static AttributeDefinition definition(AssetType assetType, String name, AttributeType type) {
        return new AttributeDefinition(assetType, name, type);
    }

    public static ConstraintDefinition constraint(AttributeDefinition definition, ConstraintDefinition.Rule rule) {
        return new ConstraintDefinition(definition, rule, null);
    }

    public static ConstraintDefinition constraint(AttributeDefinition definition, ConstraintDefinition.Rule rule, String value) {
        return new ConstraintDefinition(definition, rule, value);
    }

    public static List<ConstraintDefinition> constraints(AttributeDefinition definition, ConstraintDefinition.Rule... rules) {
        return java.util.Arrays.stream(rules)
                .map(rule -> constraint(definition, rule, null))
                .toList();
    }
}
