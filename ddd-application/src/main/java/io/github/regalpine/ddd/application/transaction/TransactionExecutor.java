package io.github.regalpine.ddd.application.transaction;

/**
 * Executes operations within a transaction boundary.
 *
 * <p>This is the application layer's view of transaction management.
 * The actual transaction implementation is provided by infrastructure.</p>
 */
public interface TransactionExecutor {

    /**
     * Executes the given callback within a transaction and returns the result.
     *
     * @param callback the transactional operation
     * @param <R> the result type
     * @return the result of the callback
     */
    <R> R execute(TransactionCallback<R> callback);

    /**
     * Executes the given runnable within a transaction.
     *
     * @param runnable the transactional operation
     */
    void execute(TransactionRunnable runnable);
}
