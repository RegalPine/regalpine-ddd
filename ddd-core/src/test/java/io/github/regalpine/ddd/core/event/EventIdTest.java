package io.github.regalpine.ddd.core.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventIdTest {

    @Test
    void shouldCreateEventIdWithValidValue() {
        var eventId = new EventId("evt-001");
        assertThat(eventId.value()).isEqualTo("evt-001");
    }

    @Test
    void shouldCreateViaFactoryMethod() {
        var eventId = EventId.of("evt-002");
        assertThat(eventId.value()).isEqualTo("evt-002");
    }

    @Test
    void shouldRejectNullValue() {
        assertThatThrownBy(() -> new EventId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be blank");
    }

    @Test
    void shouldRejectBlankValue() {
        assertThatThrownBy(() -> new EventId("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be blank");
    }

    @Test
    void shouldRejectEmptyValue() {
        assertThatThrownBy(() -> new EventId(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldSupportValueEquality() {
        assertThat(EventId.of("abc")).isEqualTo(EventId.of("abc"));
    }
}
