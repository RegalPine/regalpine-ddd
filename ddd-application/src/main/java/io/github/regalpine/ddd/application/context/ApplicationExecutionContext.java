package io.github.regalpine.ddd.application.context;

/**
 * Execution context for application-level operations.
 *
 * <p>Carries correlation and causation identifiers across
 * the command/query execution chain, along with tenant and
 * principal information for multi-tenant and security support.</p>
 */
public interface ApplicationExecutionContext {

    /**
     * Returns the unique identifier of the current command.
     *
     * @return the command ID, may be {@code null}
     */
    String commandId();

    /**
     * Returns the correlation ID linking all operations in the same business flow.
     *
     * @return the correlation ID, may be {@code null}
     */
    String correlationId();

    /**
     * Returns the causation ID of the message that directly caused this operation.
     *
     * @return the causation ID, may be {@code null}
     */
    String causationId();

    /**
     * Returns the tenant identifier for multi-tenant support.
     *
     * @return the tenant ID, may be {@code null}
     */
    String tenantId();

    /**
     * Returns the identifier of the current principal (authenticated user).
     *
     * @return the principal ID, may be {@code null}
     */
    String principalId();
}
