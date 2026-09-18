package io.github.regalpine.ddd.transaction;

/**
 * Port for adapting to specific transaction infrastructure (JDBC, JPA, etc.).
 * <p>
 * Each adapter translates framework-level transaction operations into
 * infrastructure-specific implementations.
 *
 * @author RegalPine
 */
public interface TransactionAdapter {

    /**
     * Begins a new transaction with the given definition.
     *
     * @param definition the transaction definition
     */
    void begin(TransactionDefinition definition);

    /**
     * Commits the current transaction.
     */
    void commit();

    /**
     * Rolls back the current transaction.
     */
    void rollback();

    /**
     * Returns whether a transaction is currently active.
     */
    boolean isActive();
}
