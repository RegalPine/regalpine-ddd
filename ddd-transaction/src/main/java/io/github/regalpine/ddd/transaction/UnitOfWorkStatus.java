package io.github.regalpine.ddd.transaction;

/**
 * Status of a Unit of Work lifecycle.
 *
 * <pre>{@code
 * NEW → ACTIVE → COMMITTING → COMMITTED
 *                  ↘ ROLLING_BACK → ROLLED_BACK
 * Any active state → CLOSED
 * }</pre>
 *
 * @author RegalPine
 */
public enum UnitOfWorkStatus {

    /** The Unit of Work has been created but not yet started. */
    NEW,

    /** The Unit of Work is actively tracking changes. */
    ACTIVE,

    /** The Unit of Work is in the process of committing. */
    COMMITTING,

    /** The Unit of Work has been committed successfully. */
    COMMITTED,

    /** The Unit of Work is in the process of rolling back. */
    ROLLING_BACK,

    /** The Unit of Work has been rolled back. */
    ROLLED_BACK,

    /** The Unit of Work has been closed and released all resources. */
    CLOSED
}
