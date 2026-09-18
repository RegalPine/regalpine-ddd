package io.github.regalpine.ddd.event.envelope;

import io.github.regalpine.ddd.core.event.EventId;

import java.time.Instant;
import java.util.Objects;

/**
 * Envelope wrapping an event payload with metadata for transport.
 *
 * <p>EventEnvelope belongs to Event Infrastructure, not Domain Entity.
 * It carries serialized event data together with routing, versioning,
 * and tracing metadata.</p>
 *
 * @param eventId        unique event identifier
 * @param eventType      logical event type name (e.g. "OrderPaid")
 * @param eventVersion   schema version of the event payload
 * @param aggregateType  type name of the producing aggregate
 * @param aggregateId    identifier of the producing aggregate
 * @param sequence       per-aggregate ordering sequence number
 * @param occurredAt     when the business fact occurred
 * @param payload        serialized event payload
 * @param correlationId  identifies the enclosing business process
 * @param causationId    identifies the upstream message that caused this event
 * @param tenantId       tenant identifier for multi-tenant routing
 * @param traceId        distributed tracing identifier
 * @param producer       identifier of the producing component
 * @author RegalPine
 */
public record EventEnvelope(
        EventId eventId,
        String eventType,
        int eventVersion,
        String aggregateType,
        String aggregateId,
        long sequence,
        Instant occurredAt,
        byte[] payload,
        String correlationId,
        String causationId,
        String tenantId,
        String traceId,
        String producer
) {

    public EventEnvelope {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(aggregateType, "aggregateType must not be null");
        Objects.requireNonNull(aggregateId, "aggregateId must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        Objects.requireNonNull(payload, "payload must not be null");
    }
}
