package com.db.assetstore.domain.exception.command;

import com.db.assetstore.domain.exception.DomainException;

public abstract class CommandException extends DomainException {
    protected CommandException(String message) {
        super(message);
    }

    protected CommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
