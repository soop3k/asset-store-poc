package com.db.assetstore.domain.exception.link;

public class LinkAlreadyExistsException extends LinkException {
    public LinkAlreadyExistsException(String assetId, String targetCode) {
        super("Active link already exists for asset %s and target %s".formatted(assetId, targetCode));
    }
}
