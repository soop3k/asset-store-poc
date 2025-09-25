package com.db.assetstore.domain.exception.link;

import com.db.assetstore.domain.exception.command.CommandException;

public abstract class LinkException extends CommandException {
    protected LinkException(String message) {
        super(message);
    }

    protected LinkException(String message, Throwable cause) {
        super(message, cause);
    }
}
