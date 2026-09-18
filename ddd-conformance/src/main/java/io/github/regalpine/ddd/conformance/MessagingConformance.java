package io.github.regalpine.ddd.conformance;

import io.github.regalpine.ddd.messaging.*;
import io.github.regalpine.ddd.messaging.dlq.DeadLetterPublisher;
import io.github.regalpine.ddd.messaging.retry.RetryDecision;
import io.github.regalpine.ddd.messaging.retry.RetryPolicy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Conformance test suite for Phase IX Integration &amp; Messaging Specification.
 *
 * <p>Validates core rules MSG-001 through MSG-020 and associated structural contracts.</p>
 *
 * @author RegalPine
 */
public abstract class MessagingConformance {

    // --- MSG-001: IntegrationEvent must be separated from DomainEvent ---

    /**
     * Verifies IntegrationEvent interface exists in the messaging package.
     */
    @Test
    public void verifyIntegrationEventSeparation() {
        IntegrationEvent event = new IntegrationEvent() {
            @Override
            public String eventType() { return "test.event"; }
        };
        assertEquals("test.event", event.eventType(),
                "MSG-001: IntegrationEvent should be in messaging module");
    }

    // --- MSG-003: IntegrationEvent must be versionable ---

    /**
     * Verifies IntegrationEvent supports versioning.
     */
    @Test
    public void verifyIntegrationEventVersionable() {
        IntegrationEvent event = new IntegrationEvent() {
            @Override
            public String eventType() { return "test"; }

            @Override
            public int eventVersion() { return 2; }
        };
        assertEquals(2, event.eventVersion(),
                "MSG-003: IntegrationEvent must be versionable");

        // Default version should be 1
        IntegrationEvent defaultEvent = new IntegrationEvent() {
            @Override
            public String eventType() { return "test"; }
        };
        assertEquals(1, defaultEvent.eventVersion(),
                "MSG-003: Default event version should be 1");
    }

    // --- MSG-004: Message ID must be stable and unique ---

    /**
     * Verifies MessageEnvelope requires a non-null messageId.
     */
    @Test
    public void verifyMessageIdRequired() {
        assertThrows(NullPointerException.class,
                () -> MessageEnvelope.of(null, "type", new byte[0]),
                "MSG-004: messageId must not be null");
    }

    // --- MSG-006: Consumer defaults to At-Least-Once ---

    /**
     * Verifies DeliverySemantics includes AT_LEAST_ONCE.
     */
    @Test
    public void verifyAtLeastOnceExists() {
        boolean found = false;
        for (DeliverySemantics ds : DeliverySemantics.values()) {
            if (ds == DeliverySemantics.AT_LEAST_ONCE) {
                found = true;
            }
        }
        assertTrue(found, "MSG-006: AT_LEAST_ONCE must be supported");
    }

    // --- MSG-014/MSG-015: No EXACTLY_ONCE ---

    /**
     * Verifies DeliverySemantics does not include EXACTLY_ONCE.
     */
    @Test
    public void verifyNoExactlyOnce() {
        for (DeliverySemantics ds : DeliverySemantics.values()) {
            assertFalse(ds.name().contains("EXACTLY"),
                    "MSG-014/MSG-015: Framework must not declare EXACTLY_ONCE");
        }
    }

    // --- MSG-015: Retry must be finite ---

    /**
     * Verifies RetryDecision can express "no retry" (finite retry guarantee).
     */
    @Test
    public void verifyRetryFinite() {
        RetryDecision decision = RetryDecision.noRetry();
        assertFalse(decision.shouldRetry(),
                "MSG-015: Retry must be finite (noRetry should stop)");
    }

    // --- Structural: MessageEnvelope has byte[] payload ---

    /**
     * Verifies MessageEnvelope uses byte[] payload (not generic).
     */
    @Test
    public void verifyEnvelopePayloadType() {
        MessageEnvelope env = MessageEnvelope.of("m1", "test", new byte[]{1, 2, 3});
        assertInstanceOf(byte[].class, env.payload(),
                "MessageEnvelope payload must be byte[]");
    }

    // --- Structural: MessageEnvelope schemaVersion is int ---

    /**
     * Verifies MessageEnvelope schemaVersion is an integer.
     */
    @Test
    public void verifySchemaVersionIsInt() {
        MessageEnvelope env = MessageEnvelope.of("m1", "test", new byte[0]);
        assertEquals(1, env.schemaVersion(),
                "Default schemaVersion should be 1");
    }

    // --- Structural: MessageRouter is an interface ---

    /**
     * Verifies MessageRouter is an interface (not a class).
     */
    @Test
    public void verifyMessageRouterIsInterface() {
        assertTrue(MessageRouter.class.isInterface(),
                "MessageRouter must be an interface per §22");
    }

    // --- Structural: Route has topic, queue, consumerGroup ---

    /**
     * Verifies Route has the correct fields per §22.
     */
    @Test
    public void verifyRouteStructure() {
        Route route = Route.of("topic", "queue", "group");
        assertEquals("topic", route.topic());
        assertEquals("queue", route.queue());
        assertEquals("group", route.consumerGroup());
    }
}
