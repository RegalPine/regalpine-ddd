package io.github.regalpine.ddd.messaging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class IntegrationEventTest {

    static class SampleEvent implements IntegrationEvent {
        @Override
        public String eventType() { return "order.created"; }
    }

    static class VersionedEvent implements IntegrationEvent {
        @Override
        public String eventType() { return "order.paid"; }

        @Override
        public int eventVersion() { return 3; }
    }

    @Test
    void defaultVersionShouldBeOne() {
        assertThat(new SampleEvent().eventVersion()).isEqualTo(1);
    }

    @Test
    void customVersionShouldBeRespected() {
        assertThat(new VersionedEvent().eventVersion()).isEqualTo(3);
    }

    @Test
    void eventTypeShouldBeExposed() {
        assertThat(new SampleEvent().eventType()).isEqualTo("order.created");
    }
}
