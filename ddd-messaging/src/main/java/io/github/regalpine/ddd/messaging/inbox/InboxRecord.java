package io.github.regalpine.ddd.messaging.inbox;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a record in the inbox table for idempotent message consumption.
 *
 * <p>Phase IX §28/§29: Inbox provides consumer-side idempotency.
 * The unique key is {@code consumerId + messageId} (MSG-008).</p>
 *
 * @param eventId      the event/message identifier
 * @param consumerId   the consumer/group identifier
 * @param eventType    logical event type name
 * @param receivedAt   when the event was first received
 * @param processedAt  when the event was successfully processed (null if not yet)
 * @param status       processing status (e.g. "RECEIVED", "PROCESSED", "FAILED")
 * @param attempts     number of processing attempts so far
 * @param error        last error message if processing failed (null if none)
 * @author RegalPine
 */
public record InboxRecord(
        String eventId,
        String consumerId,
        String eventType,
        Instant receivedAt,
        Instant processedAt,
        String status,
        int attempts,
        String error
) {

    public InboxRecord {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(consumerId, "consumerId must not be null");
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(receivedAt, "receivedAt must not be null");
        Objects.requireNonNull(status, "status must not be null");
    }

    /**
     * Creates a new received inbox record.
     */
    public static InboxRecord create(String eventId, String consumerId, String eventType) {
        return new InboxRecord(
                eventId, consumerId, eventType,
                Instant.now(), null, "RECEIVED", 0, null);
    }

    /**
     * Returns a new InboxRecord marked as processed.
     */
    public InboxRecord markProcessed() {
        return new InboxRecord(
                eventId, consumerId, eventType,
                receivedAt, Instant.now(), "PROCESSED", attempts, null);
    }

    /**
     * Returns a new InboxRecord with incremented attempt count and error message.
     */
    public InboxRecord recordFailure(String errorMessage) {
        return new InboxRecord(
                eventId, consumerId, eventType,
                receivedAt, processedAt, "FAILED", attempts + 1, errorMessage);
    }
}
