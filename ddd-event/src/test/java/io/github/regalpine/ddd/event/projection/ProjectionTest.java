package io.github.regalpine.ddd.event.projection;

import io.github.regalpine.ddd.core.event.*;
import io.github.regalpine.ddd.core.identifier.Identifier;
import io.github.regalpine.ddd.core.version.Version;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ProjectionTest {

    record TestIdentifier(String value) implements Identifier {}

    record SampleEvent(
            EventId eventId,
            AggregateType aggregateType,
            Identifier aggregateId,
            Version aggregateVersion,
            Instant occurredAt
    ) implements DomainEvent {}

    static class SampleProjection implements Projection<SampleEvent> {
        final List<SampleEvent> projected = new ArrayList<>();

        @Override
        public void project(SampleEvent event) {
            projected.add(event);
        }

        @Override
        public Class<SampleEvent> eventType() {
            return SampleEvent.class;
        }
    }

    @Test
    void projectShouldReceiveEvent() {
        var projection = new SampleProjection();
        var event = new SampleEvent(
                EventId.of("e1"), AggregateType.of("Order"),
                new TestIdentifier("o1"), new Version(1), Instant.now()
        );

        projection.project(event);

        assertThat(projection.projected).hasSize(1);
        assertThat(projection.projected.get(0).eventId().value()).isEqualTo("e1");
    }

    @Test
    void eventTypeShouldReturnHandledType() {
        var projection = new SampleProjection();
        assertThat(projection.eventType()).isEqualTo(SampleEvent.class);
    }
}
