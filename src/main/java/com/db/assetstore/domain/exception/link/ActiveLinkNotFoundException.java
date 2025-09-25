package com.db.assetstore.domain.exception.link;

public class ActiveLinkNotFoundException extends LinkException {
    public ActiveLinkNotFoundException(Long linkId) {
        super("Active link not found: " + linkId);
    }
}
