package io.github.regalpine.ddd.transaction;

/**
 * Callback for transactional execution returning a result.
 *
 * @param <T> the result type
 * @author RegalPine
 */
@FunctionalInterface
public interface TransactionCallback<T> {

    /**
     * Executes within a transaction context.
     *
     * @return the result
     */
    T execute();
}
