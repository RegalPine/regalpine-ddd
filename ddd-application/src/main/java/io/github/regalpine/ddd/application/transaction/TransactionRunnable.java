package io.github.regalpine.ddd.application.transaction;

/**
 * A callback that performs an operation within a transaction
 * without returning a result.
 */
@FunctionalInterface
public interface TransactionRunnable {

    /**
     * Executes the transactional operation.
     */
    void run();
}
