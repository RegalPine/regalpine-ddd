package io.github.regalpine.ddd.event.handler;

import io.github.regalpine.ddd.core.event.*;
import io.github.regalpine.ddd.core.identifier.Identifier;
import io.github.regalpine.ddd.core.version.Version;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class DomainEventDispatcherTest {

    record TestIdentifier(String value) implements Identifier {}

    record TestEvent(
            EventId eventId,
            AggregateType aggregateType,
            Identifier aggregateId,
            Version aggregateVersion,
            Instant occurredAt
    ) implements DomainEvent {}

    static class TestEventHandler implements DomainEventHandler<TestEvent> {
        final List<TestEvent> received = new ArrayList<>();

        @Override
        public void handle(TestEvent event) {
            received.add(event);
        }
    }

    private TestEvent createEvent() {
        return new TestEvent(
                EventId.of("e1"),
                AggregateType.of("TestAggregate"),
                new TestIdentifier("id-1"),
                new Version(1),
                Instant.now()
        );
    }

    @Test
    void dispatchShouldInvokeRegisteredHandler() {
        var handler = new TestEventHandler();
        var dispatcher = new DefaultDomainEventDispatcher(List.of(handler));

        dispatcher.dispatch(createEvent());

        assertThat(handler.received).hasSize(1);
    }

    @Test
    void dispatchShouldSkipWhenNoHandlerRegistered() {
        var dispatcher = new DefaultDomainEventDispatcher();

        assertThatCode(() -> dispatcher.dispatch(createEvent()))
                .doesNotThrowAnyException();
    }

    @Test
    void dispatchShouldRejectNullEvent() {
        var dispatcher = new DefaultDomainEventDispatcher();

        assertThatThrownBy(() -> dispatcher.dispatch((DomainEvent) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void registerShouldAddHandlerForEventType() {
        var handler = new TestEventHandler();
        var dispatcher = new DefaultDomainEventDispatcher();
        dispatcher.register(handler);

        dispatcher.dispatch(createEvent());

        assertThat(handler.received).hasSize(1);
    }
}
