package io.github.regalpine.ddd.event.outbox;

import io.github.regalpine.ddd.core.event.EventId;

import java.util.List;
import java.util.Objects;

/**
 * Dispatches outbox records to the messaging infrastructure.
 *
 * <p>Polls pending outbox records, publishes them via the {@link OutboxEventPublisher},
 * and marks them as published in the store.</p>
 *
 * @author RegalPine
 */
public final class OutboxDispatcher {

    private final OutboxStore outboxStore;
    private final OutboxEventPublisher publisher;

    public OutboxDispatcher(OutboxStore outboxStore, OutboxEventPublisher publisher) {
        this.outboxStore = Objects.requireNonNull(outboxStore, "outboxStore must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
    }

    /**
     * Polls and dispatches pending outbox records.
     *
     * @param limit the maximum number of records to dispatch per poll
     * @return the number of records dispatched
     */
    public int dispatchPending(int limit) {
        List<OutboxRecord> records = outboxStore.loadPending(limit);
        for (OutboxRecord record : records) {
            publisher.publish(record);
            outboxStore.markPublished(EventId.of(record.eventId()));
        }
        return records.size();
    }
}
