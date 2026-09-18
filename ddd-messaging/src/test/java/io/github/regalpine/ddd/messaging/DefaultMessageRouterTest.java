package io.github.regalpine.ddd.messaging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DefaultMessageRouterTest {

    @Test
    void shouldRouteRegisteredMessageType() {
        var router = new DefaultMessageRouter();
        var route = Route.of("domain.order", "order-queue", "order-group");
        router.register("order.created", route);

        MessageEnvelope msg = MessageEnvelope.of("m1", "order.created", new byte[0]);
        Route result = router.route(msg);

        assertThat(result.topic()).isEqualTo("domain.order");
        assertThat(result.queue()).isEqualTo("order-queue");
        assertThat(result.consumerGroup()).isEqualTo("order-group");
    }

    @Test
    void shouldThrowForUnregisteredMessageType() {
        var router = new DefaultMessageRouter();
        MessageEnvelope msg = MessageEnvelope.of("m1", "unknown.type", new byte[0]);

        assertThatThrownBy(() -> router.route(msg))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldFallbackToDefaultRoute() {
        var defaultRoute = Route.ofTopic("default-topic");
        var router = new DefaultMessageRouter(defaultRoute);
        MessageEnvelope msg = MessageEnvelope.of("m1", "any.type", new byte[0]);

        assertThat(router.route(msg).topic()).isEqualTo("default-topic");
    }
}
