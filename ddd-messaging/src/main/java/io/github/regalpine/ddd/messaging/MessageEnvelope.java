package io.github.regalpine.ddd.messaging;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Envelope wrapping a message payload with metadata for transport.
 *
 * <p>Phase IX §7: Unified message envelope with all metadata fields required
 * for cross-boundary message interoperability.</p>
 *
 * @param messageId      globally unique, stable, not reused across retries (MSG-004)
 * @param messageType    stable event type name (e.g. "order.created")
 * @param schemaVersion  schema version for contract evolution (§14)
 * @param source         originating service identifier (e.g. "service://order") (§11)
 * @param subject        message target (e.g. "order/12345") (§12)
 * @param aggregateType  producing aggregate type name
 * @param aggregateId    producing aggregate identifier
 * @param sequence       per-aggregate ordering sequence (§41)
 * @param occurredAt     when the business fact occurred
 * @param publishedAt    when the message was published
 * @param correlationId  business flow correlation (§9)
 * @param causationId    direct causal message (§10)
 * @param tenantId       tenant identifier for multi-tenant routing (§54)
 * @param headers        extensible transport headers (§73)
 * @param payload        serialized message payload (§7)
 * @author RegalPine
 */
public record MessageEnvelope(
        String messageId,
        String messageType,
        int schemaVersion,
        String source,
        String subject,
        String aggregateType,
        String aggregateId,
        String sequence,
        Instant occurredAt,
        Instant publishedAt,
        String correlationId,
        String causationId,
        String tenantId,
        Map<String, String> headers,
        byte[] payload
) {

    public MessageEnvelope {
        Objects.requireNonNull(messageId, "messageId must not be null");
        Objects.requireNonNull(messageType, "messageType must not be null");
        Objects.requireNonNull(payload, "payload must not be null");
        headers = headers != null ? Map.copyOf(headers) : Map.of();
    }

    /**
     * Creates a minimal envelope with just ID, type, and payload.
     */
    public static MessageEnvelope of(String messageId, String messageType, byte[] payload) {
        Instant now = Instant.now();
        return new MessageEnvelope(
                messageId, messageType, 1,
                null, null, null, null, null,
                now, now, null, null, null, Map.of(), payload);
    }

    /**
     * Creates an envelope with correlation and causation IDs.
     */
    public static MessageEnvelope of(
            String messageId, String messageType, byte[] payload,
            String correlationId, String causationId) {
        Instant now = Instant.now();
        return new MessageEnvelope(
                messageId, messageType, 1,
                null, null, null, null, null,
                now, now, correlationId, causationId, null, Map.of(), payload);
    }
}
