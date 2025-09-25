package com.db.assetstore.domain.exception.link;

public class LinkDefinitionNotFoundException extends LinkException {
    public LinkDefinitionNotFoundException(String entityType, String entitySubtype) {
        super("Link definition missing for %s/%s".formatted(entityType, entitySubtype));
    }
}
