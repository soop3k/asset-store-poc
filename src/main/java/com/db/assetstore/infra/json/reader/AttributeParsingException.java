package com.db.assetstore.infra.json.reader;

import com.db.assetstore.domain.model.type.AttributeType;

public class AttributeParsingException extends RuntimeException {

    private AttributeParsingException(String message) {
        super(message);
    }

    static AttributeParsingException invalidPayload() {
        return new AttributeParsingException("Attributes payload must be a JSON object");
    }

    static AttributeParsingException missingDefinition(String attributeName) {
        return new AttributeParsingException("Unknown attribute definition: " + attributeName);
    }

    static AttributeParsingException incompatibleType(String attributeName,
                                                      AttributeType expected,
                                                      String actual) {
        return new AttributeParsingException(
                "Attribute '" + attributeName + "' expects type " + expected + " but received " + actual
        );
    }
}
