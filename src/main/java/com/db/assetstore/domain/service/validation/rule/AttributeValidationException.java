package com.db.assetstore.domain.service.validation.rule;

public class AttributeValidationException extends RuntimeException {

    public AttributeValidationException(String message) {
        super(message);
    }

    public AttributeValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
