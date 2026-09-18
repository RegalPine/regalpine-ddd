package io.github.regalpine.ddd.event.outbox;

/**
 * Port for publishing outbox records to messaging infrastructure.
 *
 * @author RegalPine
 */
public interface OutboxEventPublisher {

    /**
     * Publishes the given outbox record to the messaging infrastructure.
     *
     * @param record the outbox record to publish
     */
    void publish(OutboxRecord record);
}
