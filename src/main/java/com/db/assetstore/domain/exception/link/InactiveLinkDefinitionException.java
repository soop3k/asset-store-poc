package com.db.assetstore.domain.exception.link;

public class InactiveLinkDefinitionException extends LinkException {
    public InactiveLinkDefinitionException(String entityType, String entitySubtype) {
        super("Link definition inactive for %s/%s".formatted(entityType, entitySubtype));
    }
}
