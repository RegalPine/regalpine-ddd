package io.github.regalpine.ddd.conformance;

import io.github.regalpine.ddd.event.outbox.OutboxRecord;
import io.github.regalpine.ddd.event.outbox.OutboxStore;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Conformance test suite for {@link OutboxStore} implementations.
 *
 * @author RegalPine
 */
public abstract class OutboxStoreConformance {

    /**
     * Subclasses must provide the store under test.
     */
    protected abstract OutboxStore store();

    /**
     * Creates a test outbox record.
     */
    protected OutboxRecord createRecord() {
        return OutboxRecord.create(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "TestEvent",
                1,
                "TestAggregate",
                "test-id-1",
                "{\"test\": true}",
                Instant.now()
        );
    }

    /**
     * Verifies append and loadPending behavior.
     */
    @Test
    public void verifyAppendAndLoad() {
        OutboxRecord record = createRecord();
        store().append(record);
        List<OutboxRecord> found = store().loadPending(10);
        assertFalse(found.isEmpty(), "Should find pending record after append");
    }

    /**
     * Verifies markPublished behavior.
     */
    @Test
    public void verifyMarkPublished() {
        OutboxRecord record = createRecord();
        store().append(record);
        store().markPublished(io.github.regalpine.ddd.core.event.EventId.of(record.eventId()));
        List<OutboxRecord> found = store().loadPending(10);
        boolean stillPresent = found.stream().anyMatch(r -> r.outboxId().equals(record.outboxId()));
        assertFalse(stillPresent, "Record should not appear as pending after markPublished");
    }
}
