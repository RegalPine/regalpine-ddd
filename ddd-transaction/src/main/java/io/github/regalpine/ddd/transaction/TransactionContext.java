package io.github.regalpine.ddd.transaction;

/**
 * Represents the context of a transaction at runtime.
 * <p>
 * TransactionContext must not pollute the Domain layer. It belongs to
 * the Application / Infrastructure boundary.
 *
 * @author RegalPine
 */
public interface TransactionContext {

    /**
     * Returns whether a transaction is currently active.
     */
    boolean active();

    /**
     * Returns whether the current transaction is read-only.
     */
    boolean readOnly();

    /**
     * Returns the isolation level of the current transaction.
     */
    TransactionIsolation isolation();
}
