package com.db.assetstore.domain.service.cmd;

import java.util.Objects;

/**
 * Encapsulates the outcome of executing a command together with the asset identifier used for logging.
 *
 * @param <T> result type produced by the command execution
 */
public record CommandResult<T>(T result, String assetId, String executedBy, boolean success) {

    public CommandResult {
        Objects.requireNonNull(assetId, "assetId");
        Objects.requireNonNull(executedBy, "executedBy");
        if (executedBy.isBlank()) {
            throw new IllegalArgumentException("executedBy must not be blank");
        }
    }

    public CommandResult(T result, String assetId, String executedBy) {
        this(result, assetId, executedBy, true);
    }

    public static CommandResult<Void> noResult(String assetId, String executedBy) {
        return new CommandResult<>(null, assetId, executedBy, true);
    }

    public static CommandResult<Void> failure(String assetId, String executedBy) {
        return new CommandResult<>(null, assetId, executedBy, false);
    }
}
