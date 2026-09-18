package io.github.regalpine.ddd.messaging.inbox;

import java.util.Optional;

/**
 * Port for inbox-based idempotent message consumption.
 *
 * <p>Phase IX §28: InboxStore provides at-least-once delivery guarantee by tracking
 * processed messages per consumer. The unique key is {@code consumerId + messageId} (MSG-008).</p>
 *
 * <p>Phase IX §29: Inbox and business processing must share a consistency boundary.</p>
 *
 * @author RegalPine
 */
public interface InboxStore {

    /**
     * Checks if a message has already been processed by the given consumer.
     *
     * <p>Phase IX §28 spec signature: {@code exists(consumerId, messageId)}.</p>
     *
     * @param consumerId the consumer identifier
     * @param messageId  the message/event identifier
     * @return true if the message has been processed by this consumer
     */
    boolean exists(String consumerId, String messageId);

    /** 同一业务事务内原子占位；重复键返回 false，不使事务失效。 */
    default boolean tryInsert(InboxRecord record) {
        throw new UnsupportedOperationException("InboxStore 未实现原子占位");
    }

    /**
     * Records that a message has been received/processed by the given consumer.
     *
     * <p>Phase IX §28 spec signature: {@code record(consumerId, messageId)}.</p>
     *
     * @param consumerId the consumer identifier
     * @param messageId  the message/event identifier
     */
    void record(String consumerId, String messageId);

    // --- Extended API for full lifecycle management ---

    /**
     * Checks if an event has already been processed by the given consumer.
     * Alias for {@link #exists(String, String)}.
     *
     * @param consumerId the consumer identifier
     * @param eventId    the event identifier
     * @return true if the event has been processed by this consumer
     */
    default boolean isProcessed(String consumerId, String eventId) {
        return exists(consumerId, eventId);
    }

    /**
     * Saves an inbox record.
     *
     * @param record the record to save
     */
    void save(InboxRecord record);

    /**
     * Marks an event as processed for the given consumer.
     *
     * @param consumerId the consumer identifier
     * @param eventId    the event identifier
     */
    void markProcessed(String consumerId, String eventId);

    /**
     * Finds an inbox record by consumer and event ID.
     *
     * @param consumerId the consumer identifier
     * @param eventId    the event identifier
     * @return the record, or empty if not found
     */
    Optional<InboxRecord> findBy(String consumerId, String eventId);
}
