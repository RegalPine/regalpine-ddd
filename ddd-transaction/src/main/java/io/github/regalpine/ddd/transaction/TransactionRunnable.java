package io.github.regalpine.ddd.transaction;

/**
 * Callback for transactional execution without a result.
 *
 * @author RegalPine
 */
@FunctionalInterface
public interface TransactionRunnable {

    /**
     * Executes within a transaction context.
     */
    void run();
}
