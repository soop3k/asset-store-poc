package com.db.assetstore.domain.service.link;

/**
 * Exception thrown when asset link domain validation fails.
 */
public class AssetLinkException extends RuntimeException {

    public AssetLinkException(String message) {
        super(message);
    }

    public AssetLinkException(String message, Throwable cause) {
        super(message, cause);
    }
}
