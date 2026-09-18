package io.github.regalpine.ddd.transaction;

/**
 * Callback interface for transaction lifecycle synchronization.
 * <p>
 * These callbacks belong to the Application / Infrastructure layer.
 * Domain code must not depend on this interface.
 *
 * @author RegalPine
 */
public interface TransactionSynchronization {

    /**
     * Called before the transaction is committed.
     */
    void beforeCommit();

    /**
     * Called after the transaction has been committed successfully.
     */
    void afterCommit();

    /**
     * Called after the transaction has been rolled back.
     */
    void afterRollback();
}
