package io.github.regalpine.ddd.conformance;

import io.github.regalpine.ddd.messaging.inbox.InboxRecord;
import io.github.regalpine.ddd.messaging.inbox.InboxStore;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Conformance test suite for {@link InboxStore} implementations.
 *
 * @author RegalPine
 */
public abstract class InboxStoreConformance {

    /**
     * Subclasses must provide the store under test.
     */
    protected abstract InboxStore store();

    private final String consumerId = "test-consumer";

    /**
     * Verifies exists returns false for unknown events.
     */
    @Test
    public void verifyUnknownNotProcessed() {
        assertFalse(store().exists(consumerId, "non-existent-id"),
                "Unknown event should not be recorded");
    }

    /**
     * Verifies save and markProcessed lifecycle.
     */
    @Test
    public void verifySaveAndMarkProcessed() {
        String eventId = UUID.randomUUID().toString();
        InboxRecord record = InboxRecord.create(eventId, consumerId, "TestEvent");
        store().save(record);

        assertFalse(store().exists(consumerId, eventId),
                "Event should not be processed before markProcessed");

        store().markProcessed(consumerId, eventId);

        assertTrue(store().exists(consumerId, eventId),
                "Event should be processed after markProcessed");
    }
}
