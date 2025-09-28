package com.db.assetstore.infra.json.reader;

import com.db.assetstore.domain.model.type.AttributeType;

public class AttributeParsingException extends RuntimeException {

    private AttributeParsingException(String message) {
        super(message);
    }

    static AttributeParsingException invalidValue(String name, String value, AttributeType type) {
        return new AttributeParsingException(String.format(
                "Attributes has invalid value for type [name =%s, type=%s, value=%s]", name, type, value));
    }

    static AttributeParsingException missingDefinition(String name) {
        return new AttributeParsingException(String.format("Unknown attribute definition [name=%s]", name));
    }

    static AttributeParsingException incompatibleType(String attributeName,
                                                      AttributeType expected,
                                                      String actual) {
        return new AttributeParsingException(
                String.format("Attribute type mismatch [name= %s expected=%s actual=%s]", attributeName,  expected, actual)
        );
    }
}
