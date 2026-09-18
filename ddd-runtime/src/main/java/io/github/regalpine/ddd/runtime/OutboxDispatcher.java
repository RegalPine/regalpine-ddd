package io.github.regalpine.ddd.runtime;

/**
 * Dispatches outbox records in batches after transaction commit.
 *
 * <p>The outbox dispatcher loads pending outbox records, publishes them
 * to the message broker, and marks them as published. Failed publishes
 * leave the record pending for retry.</p>
 *
 * @author RegalPine
 */
public interface OutboxDispatcher {

    /**
     * Dispatches a batch of pending outbox records.
     *
     * @param batchSize the maximum number of records to dispatch
     * @return the dispatch result with counts of dispatched, failed, and remaining records
     */
    DispatchResult dispatchBatch(int batchSize);
}
