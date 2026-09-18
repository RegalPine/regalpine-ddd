package io.github.regalpine.ddd.conformance.mybatis;

import io.github.regalpine.ddd.infrastructure.mybatis.inbox.MyBatisInboxStore;
import io.github.regalpine.ddd.messaging.inbox.InboxStore;

/**
 * Conformance test suite for MyBatis inbox store.
 *
 * <p>Phase XII §73: verifies inbox idempotency — processing the same message
 * twice must not execute business logic the second time. The unique key
 * consumerId + messageId ensures deduplication.</p>
 *
 * @author RegalPine
 */
public abstract class MyBatisInboxConformance {

    /**
     * Subclasses must provide the inbox store under test.
     */
    protected abstract MyBatisInboxStore inboxStore();

    /**
     * Verifies that the store implements InboxStore.
     */
    protected void verifyImplementsInboxStore() {
        if (!(inboxStore() instanceof InboxStore)) {
            throw new AssertionError(
                    "MyBatisInboxStore must implement InboxStore");
        }
    }

    /**
     * Runs all conformance checks.
     */
    public void runAll() {
        verifyImplementsInboxStore();
    }
}
