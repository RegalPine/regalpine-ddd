package io.github.regalpine.ddd.event.envelope;

import io.github.regalpine.ddd.core.event.EventId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class EventEnvelopeTest {

    @Test
    void shouldCreateWithAllFields() {
        var now = Instant.now();
        var envelope = new EventEnvelope(
                EventId.of("e1"), "OrderPaid", 2,
                "Order", "order-100", 10L,
                now, "{\"amount\":100}".getBytes(),
                "corr-1", "cause-1", "tenant-1", "trace-1", "producer-1"
        );

        assertThat(envelope.eventId().value()).isEqualTo("e1");
        assertThat(envelope.eventType()).isEqualTo("OrderPaid");
        assertThat(envelope.eventVersion()).isEqualTo(2);
        assertThat(envelope.aggregateType()).isEqualTo("Order");
        assertThat(envelope.aggregateId()).isEqualTo("order-100");
        assertThat(envelope.sequence()).isEqualTo(10L);
        assertThat(envelope.occurredAt()).isEqualTo(now);
        assertThat(envelope.payload()).isNotEmpty();
        assertThat(envelope.correlationId()).isEqualTo("corr-1");
        assertThat(envelope.causationId()).isEqualTo("cause-1");
        assertThat(envelope.tenantId()).isEqualTo("tenant-1");
        assertThat(envelope.traceId()).isEqualTo("trace-1");
        assertThat(envelope.producer()).isEqualTo("producer-1");
    }

    @Test
    void shouldRejectNullEventId() {
        assertThatThrownBy(() -> new EventEnvelope(
                null, "type", 1, "agg", "id", 1L,
                Instant.now(), new byte[0],
                null, null, null, null, null
        )).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullPayload() {
        assertThatThrownBy(() -> new EventEnvelope(
                EventId.of("e1"), "type", 1, "agg", "id", 1L,
                Instant.now(), null,
                null, null, null, null, null
        )).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldAllowNullOptionalMetadata() {
        var envelope = new EventEnvelope(
                EventId.of("e1"), "type", 1, "agg", "id", 1L,
                Instant.now(), new byte[0],
                null, null, null, null, null
        );

        assertThat(envelope.correlationId()).isNull();
        assertThat(envelope.causationId()).isNull();
        assertThat(envelope.tenantId()).isNull();
        assertThat(envelope.traceId()).isNull();
        assertThat(envelope.producer()).isNull();
    }
}
