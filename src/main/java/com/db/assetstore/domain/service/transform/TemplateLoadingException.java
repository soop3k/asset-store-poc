package com.db.assetstore.domain.service.transform;

/**
 * Exception thrown when template loading or compilation fails.
 * Provides specific error handling for JSLT template-related issues.
 */
public class TemplateLoadingException extends RuntimeException {
    
    public TemplateLoadingException(String message) {
        super(message);
    }
    
    public TemplateLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}