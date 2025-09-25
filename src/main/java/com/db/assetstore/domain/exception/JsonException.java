package com.db.assetstore.domain.exception;

public abstract class JsonException extends DomainException {
    protected JsonException(String message) {
        super(message);
    }

    protected JsonException(String message, Throwable cause) {
        super(message, cause);
    }
}
