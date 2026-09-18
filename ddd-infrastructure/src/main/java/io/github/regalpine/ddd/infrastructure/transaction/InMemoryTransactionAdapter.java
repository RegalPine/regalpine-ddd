package io.github.regalpine.ddd.infrastructure.transaction;

import io.github.regalpine.ddd.transaction.*;

import java.util.Objects;

/**
 * In-memory implementation of {@link TransactionAdapter} for testing.
 * <p>
 * Simulates transaction lifecycle without real infrastructure.
 *
 * @author RegalPine
 */
public final class InMemoryTransactionAdapter implements TransactionAdapter {

    private volatile boolean active;
    private volatile boolean failOnCommit;

    @Override
    public void begin(TransactionDefinition definition) {
        if (active) {
            throw new IllegalStateException("Transaction already active");
        }
        active = true;
    }

    @Override
    public void commit() {
        if (!active) {
            throw new IllegalStateException("No active transaction to commit");
        }
        if (failOnCommit) {
            failOnCommit = false;
            active = false;
            throw new RuntimeException("Simulated commit failure");
        }
        active = false;
    }

    @Override
    public void rollback() {
        active = false;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    /**
     * Configures the adapter to fail on the next commit (for testing).
     */
    public void setFailOnCommit(boolean fail) {
        this.failOnCommit = fail;
    }
}
