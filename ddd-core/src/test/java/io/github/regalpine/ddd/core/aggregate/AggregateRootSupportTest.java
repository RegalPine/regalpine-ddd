package io.github.regalpine.ddd.core.aggregate;

import io.github.regalpine.ddd.core.event.AggregateType;
import io.github.regalpine.ddd.core.event.DomainEvent;
import io.github.regalpine.ddd.core.event.EventId;
import io.github.regalpine.ddd.core.identifier.Identifier;
import io.github.regalpine.ddd.core.version.Version;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AggregateRootSupportTest {

    // -- Test fixtures --

    record TestId(String value) implements Identifier {
        @Override
        public String value() {
            return value;
        }
    }

    record TestEvent(
            EventId eventId,
            AggregateType aggregateType,
            TestId aggregateId,
            Version aggregateVersion,
            Instant occurredAt,
            String data
    ) implements DomainEvent {}

    static class TestAggregate extends AggregateRootSupport<TestId> {
        private final TestId id;
        private Version version;

        TestAggregate(TestId id) {
            this.id = id;
            this.version = Version.initial();
        }

        @Override
        public TestId id() {
            return id;
        }

        @Override
        public Version version() {
            return version;
        }

        void doSomething() {
            raise(new TestEvent(
                    EventId.of("evt-1"),
                    AggregateType.of("test"),
                    id,
                    version,
                    Instant.now(),
                    "something happened"
            ));
        }
    }

    // -- Tests --

    @Test
    void shouldReturnIdentity() {
        var aggregate = new TestAggregate(new TestId("agg-1"));
        assertThat(aggregate.id().value()).isEqualTo("agg-1");
    }

    @Test
    void shouldStartWithInitialVersion() {
        var aggregate = new TestAggregate(new TestId("agg-1"));
        assertThat(aggregate.version()).isEqualTo(Version.initial());
    }

    @Test
    void shouldHaveNoDomainEventsInitially() {
        var aggregate = new TestAggregate(new TestId("agg-1"));
        assertThat(aggregate.domainEvents()).isEmpty();
    }

    @Test
    void shouldCollectDomainEventsOnRaise() {
        var aggregate = new TestAggregate(new TestId("agg-1"));
        aggregate.doSomething();

        assertThat(aggregate.domainEvents()).hasSize(1);
        assertThat(aggregate.domainEvents().get(0)).isInstanceOf(TestEvent.class);
    }

    @Test
    void shouldClearDomainEvents() {
        var aggregate = new TestAggregate(new TestId("agg-1"));
        aggregate.doSomething();
        assertThat(aggregate.domainEvents()).hasSize(1);

        aggregate.clearDomainEvents();
        assertThat(aggregate.domainEvents()).isEmpty();
    }

    @Test
    void shouldReturnUnmodifiableDomainEvents() {
        var aggregate = new TestAggregate(new TestId("agg-1"));
        aggregate.doSomething();

        assertThatThrownBy(() -> aggregate.domainEvents().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldRejectNullEventInRaise() {
        var aggregate = new TestAggregate(new TestId("agg-1"));
        assertThatThrownBy(() -> aggregate.raise(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("must not be null");
    }
}
