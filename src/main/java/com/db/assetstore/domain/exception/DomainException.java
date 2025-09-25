package com.db.assetstore.domain.exception;

public class DomainException extends Exception {
    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
