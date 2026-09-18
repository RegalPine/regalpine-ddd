package io.github.regalpine.ddd.transaction;

/**
 * Port for managing transactions.
 * <p>
 * Infrastructure adapters implement this interface to provide transaction
 * management capabilities (begin, commit, rollback).
 *
 * @author RegalPine
 */
public interface TransactionManager {

    /**
     * Executes a callback within a transaction using the default definition.
     *
     * @param callback the transactional callback
     * @param <T>      the result type
     * @return the result
     */
    <T> T execute(TransactionCallback<T> callback);

    /**
     * Executes a callback within a transaction using the given definition.
     *
     * @param definition the transaction definition
     * @param callback   the transactional callback
     * @param <T>        the result type
     * @return the result
     */
    <T> T execute(TransactionDefinition definition, TransactionCallback<T> callback);

    /**
     * Executes a runnable within a transaction using the default definition.
     *
     * @param runnable the transactional runnable
     */
    void execute(TransactionRunnable runnable);

    /**
     * Executes a runnable within a transaction using the given definition.
     *
     * @param definition the transaction definition
     * @param runnable   the transactional runnable
     */
    void execute(TransactionDefinition definition, TransactionRunnable runnable);

    default boolean isActive() { return false; }

    default void setRollbackOnly() {
        throw new IllegalStateException("没有可标记的事务");
    }

    default void registerSynchronization(TransactionSynchronization synchronization) {
        throw new IllegalStateException("没有可注册回调的事务");
    }
}
