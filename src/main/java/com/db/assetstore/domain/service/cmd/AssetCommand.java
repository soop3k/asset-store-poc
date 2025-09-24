package com.db.assetstore.domain.service.cmd;

import java.util.Objects;

/**
 * Marker interface for commands executed by the {@link com.db.assetstore.domain.service.AssetCommandService}.
 * Provides the command name so infrastructure components can log or persist it without hardcoded values.
 */
public interface AssetCommand<R> {

    /**
     * Applies the visitor to the command so it can execute its behaviour.
     *
     * @param visitor visitor handling the execution for the command type
     * @return command result produced by the visitor
     */
    CommandResult<R> accept(AssetCommandVisitor visitor);

    /**
     * Returns the logical name of the command.
     * Default implementation delegates to the concrete class simple name.
     *
     * @return command type identifier
     */
    default String commandType() {
        return getClass().getSimpleName();
    }

    /**
     * Identifies the user or system that executed the command.
     *
     * @return executor identifier, may be {@code null} when unknown
     */
    String executedBy();

    /**
     * Utility method for implementors to guard against null visitors.
     */
    default AssetCommandVisitor requireVisitor(AssetCommandVisitor visitor) {
        return Objects.requireNonNull(visitor, "visitor");
    }
}
