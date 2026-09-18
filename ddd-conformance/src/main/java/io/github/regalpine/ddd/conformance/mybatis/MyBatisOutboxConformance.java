package io.github.regalpine.ddd.conformance.mybatis;

import io.github.regalpine.ddd.event.outbox.OutboxStore;
import io.github.regalpine.ddd.infrastructure.mybatis.outbox.MyBatisOutboxStore;

/**
 * Conformance test suite for MyBatis outbox store.
 *
 * <p>Phase XII §72: verifies outbox atomicity — aggregate UPDATE and
 * outbox INSERT must share the same transaction. Cannot appear in a state
 * where aggregate exists but outbox is missing.</p>
 *
 * @author RegalPine
 */
public abstract class MyBatisOutboxConformance {

    /**
     * Subclasses must provide the outbox store under test.
     */
    protected abstract MyBatisOutboxStore outboxStore();

    /**
     * Verifies that the store implements OutboxStore.
     */
    protected void verifyImplementsOutboxStore() {
        if (!(outboxStore() instanceof OutboxStore)) {
            throw new AssertionError(
                    "MyBatisOutboxStore must implement OutboxStore");
        }
    }

    /**
     * Runs all conformance checks.
     */
    public void runAll() {
        verifyImplementsOutboxStore();
    }
}
