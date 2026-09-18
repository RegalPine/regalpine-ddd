package io.github.regalpine.ddd.event.outbox;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a record in the outbox table for transactional event publishing.
 *
 * <p>Each record captures a domain event that has been committed within a transaction
 * but not yet published to the messaging infrastructure. Fields align with the
 * recommended logical model (Phase V §31).</p>
 *
 * @param outboxId      unique outbox record identifier
 * @param eventId       the domain event identifier
 * @param eventType     logical event type name (e.g. "OrderPaid")
 * @param eventVersion  schema version of the event payload
 * @param aggregateType type name of the producing aggregate
 * @param aggregateId   identifier of the producing aggregate
 * @param tenantId      tenant identifier for multi-tenant event routing (nullable)
 * @param payload       serialized event payload
 * @param occurredAt    when the business fact occurred
 * @param createdAt     when the outbox record was created
 * @param status        delivery status (e.g. "PENDING", "PUBLISHED", "FAILED")
 * @param attempts      number of delivery attempts so far
 * @param publishedAt   when the event was successfully published (null if not yet published)
 * @author RegalPine
 */
public record OutboxRecord(
        String outboxId,
        String eventId,
        String eventType,
        int eventVersion,
        String aggregateType,
        String aggregateId,
        String tenantId,
        String payload,
        Instant occurredAt,
        Instant createdAt,
        String status,
        int attempts,
        Instant publishedAt,
        String destination,
        Instant nextAttemptAt,
        String leaseToken,
        Instant leaseUntil,
        String lastError
) {
    public OutboxRecord(String outboxId, String eventId, String eventType, int eventVersion,
                        String aggregateType, String aggregateId, String tenantId, String payload,
                        Instant occurredAt, Instant createdAt, String status, int attempts, Instant publishedAt) {
        this(outboxId, eventId, eventType, eventVersion, aggregateType, aggregateId, tenantId, payload,
                occurredAt, createdAt, status, attempts, publishedAt, eventType, createdAt, null, null, null);
    }

    public OutboxRecord {
        Objects.requireNonNull(outboxId, "outboxId must not be null");
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(aggregateType, "aggregateType must not be null");
        Objects.requireNonNull(aggregateId, "aggregateId must not be null");
        Objects.requireNonNull(payload, "payload must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(status, "status must not be null");
    }

    /**
     * Creates a new pending outbox record without tenant context.
     */
    public static OutboxRecord create(
            String outboxId,
            String eventId,
            String eventType,
            int eventVersion,
            String aggregateType,
            String aggregateId,
            String payload,
            Instant occurredAt) {
        return new OutboxRecord(
                outboxId, eventId, eventType, eventVersion,
                aggregateType, aggregateId, null, payload,
                occurredAt, Instant.now(), "PENDING", 0, null);
    }

    /**
     * Creates a new pending outbox record with tenant context.
     *
     * @param tenantId the tenant identifier (IS-05 §14)
     */
    public static OutboxRecord createWithTenant(
            String outboxId,
            String eventId,
            String eventType,
            int eventVersion,
            String aggregateType,
            String aggregateId,
            String tenantId,
            String payload,
            Instant occurredAt) {
        return new OutboxRecord(
                outboxId, eventId, eventType, eventVersion,
                aggregateType, aggregateId, tenantId, payload,
                occurredAt, Instant.now(), "PENDING", 0, null);
    }

    /**
     * Returns a new OutboxRecord marked as published.
     */
    public OutboxRecord markPublished() {
        return new OutboxRecord(
                outboxId, eventId, eventType, eventVersion,
                aggregateType, aggregateId, tenantId, payload,
                occurredAt, createdAt, "PUBLISHED", attempts, Instant.now(), destination,
                nextAttemptAt, null, null, lastError);
    }

    /**
     * Returns a new OutboxRecord with incremented attempt count.
     */
    public OutboxRecord incrementAttempts() {
        return new OutboxRecord(
                outboxId, eventId, eventType, eventVersion,
                aggregateType, aggregateId, tenantId, payload,
                occurredAt, createdAt, status, attempts + 1, publishedAt, destination,
                nextAttemptAt, leaseToken, leaseUntil, lastError);
    }
}
