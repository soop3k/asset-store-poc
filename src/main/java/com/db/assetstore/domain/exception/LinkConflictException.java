package com.db.assetstore.domain.exception;

public class LinkConflictException extends RuntimeException {
    public LinkConflictException(String message) {
        super(message);
    }
}
