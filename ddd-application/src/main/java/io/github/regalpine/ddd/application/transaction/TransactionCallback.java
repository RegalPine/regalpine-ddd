package io.github.regalpine.ddd.application.transaction;

/**
 * A callback that produces a result within a transaction.
 *
 * @param <T> the result type
 */
@FunctionalInterface
public interface TransactionCallback<T> {

    /**
     * Executes the transactional operation.
     *
     * @return the result
     */
    T execute();
}
