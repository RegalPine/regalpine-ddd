package io.github.regalpine.ddd.messaging;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class MessageEnvelopeTest {

    @Test
    void ofShouldCreateMinimalEnvelope() {
        byte[] payload = "{\"id\":\"1\"}".getBytes();
        MessageEnvelope env = MessageEnvelope.of("m1", "order.created", payload);

        assertThat(env.messageId()).isEqualTo("m1");
        assertThat(env.messageType()).isEqualTo("order.created");
        assertThat(env.schemaVersion()).isEqualTo(1);
        assertThat(env.payload()).isEqualTo(payload);
        assertThat(env.occurredAt()).isNotNull();
        assertThat(env.publishedAt()).isNotNull();
        assertThat(env.headers()).isEmpty();
    }

    @Test
    void ofWithCorrelationShouldSetIds() {
        byte[] payload = "test".getBytes();
        MessageEnvelope env = MessageEnvelope.of("m1", "order.paid", payload, "corr-1", "cause-1");

        assertThat(env.correlationId()).isEqualTo("corr-1");
        assertThat(env.causationId()).isEqualTo("cause-1");
    }

    @Test
    void shouldRejectNullMessageId() {
        assertThatThrownBy(() -> MessageEnvelope.of(null, "t", new byte[0]))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullPayload() {
        assertThatThrownBy(() -> MessageEnvelope.of("m1", "t", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void headersShouldBeImmutable() {
        MessageEnvelope env = new MessageEnvelope(
                "m1", "t", 1, null, null, null, null, null,
                Instant.now(), Instant.now(), null, null, null,
                Map.of("key", "value"), new byte[0]);

        assertThat(env.headers()).containsEntry("key", "value");
        assertThatThrownBy(() -> env.headers().put("x", "y"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
