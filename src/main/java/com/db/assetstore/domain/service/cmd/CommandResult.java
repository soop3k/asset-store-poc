package com.db.assetstore.domain.service.cmd;

import org.springframework.lang.NonNull;

/**
 * Encapsulates the outcome of executing a command together with the asset identifier used for logging.
 *
 * @param <T> result type produced by the command execution
 */
public record CommandResult<T>(T result, @NonNull String assetId, boolean success) {

    public CommandResult(T result, @NonNull String assetId) {
        this(result, assetId, true);
    }

    public static CommandResult<Void> noResult(@NonNull String assetId) {
        return new CommandResult<>(null, assetId, true);
    }

    public static CommandResult<Void> failure(@NonNull String assetId) {
        return new CommandResult<>(null, assetId, false);
    }
}
