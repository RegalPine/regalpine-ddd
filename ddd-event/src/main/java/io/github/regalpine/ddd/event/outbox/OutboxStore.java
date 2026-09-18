package io.github.regalpine.ddd.event.outbox;

import io.github.regalpine.ddd.core.event.EventId;

import java.util.List;

/**
 * Port for persisting and querying outbox records.
 *
 * <p>Implements the Outbox Pattern: domain events are written to an outbox table
 * within the same transaction as the aggregate save, then dispatched asynchronously.</p>
 *
 * @author RegalPine
 */
public interface OutboxStore {

    /**
     * Appends an outbox record to the store.
     *
     * @param record the record to append
     */
    void append(OutboxRecord record);

    /**
     * Loads pending (un-published) outbox records up to the given limit.
     *
     * @param limit the maximum number of records to return
     * @return list of pending records
     */
    List<OutboxRecord> loadPending(int limit);

    /**
     * Marks an outbox record as published by its event ID.
     *
     * @param eventId the event identifier
     */
    void markPublished(EventId eventId);
}
