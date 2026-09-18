package io.github.regalpine.ddd.domain.repository;

import io.github.regalpine.ddd.core.aggregate.AggregateRootSupport;
import io.github.regalpine.ddd.core.identifier.Identifier;
import io.github.regalpine.ddd.core.version.Version;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AggregateRepositoryTest {

    // -- Test fixtures --

    record TestId(String value) implements Identifier {
        @Override
        public String value() {
            return value;
        }
    }

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
    }

    /**
     * Simple in-memory repository for testing within ddd-domain.
     */
    static class InMemoryRepo implements AggregateRepository<TestAggregate, TestId> {
        private final Map<String, TestAggregate> store = new ConcurrentHashMap<>();

        @Override
        public Optional<TestAggregate> findById(TestId id) {
            Objects.requireNonNull(id, "id must not be null");
            return Optional.ofNullable(store.get(id.value()));
        }

        @Override
        public TestAggregate save(TestAggregate aggregate) {
            Objects.requireNonNull(aggregate, "aggregate must not be null");
            store.put(aggregate.id().value(), aggregate);
            return aggregate;
        }

        @Override
        public void delete(TestAggregate aggregate) {
            Objects.requireNonNull(aggregate, "aggregate must not be null");
            store.remove(aggregate.id().value());
        }
    }

    private InMemoryRepo repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRepo();
    }

    // -- Tests --

    @Test
    void shouldReturnEmptyWhenNotFound() {
        var id = new TestId("non-existent");
        assertThat(repository.findById(id)).isEmpty();
    }

    @Test
    void shouldFindSavedAggregate() {
        var aggregate = new TestAggregate(new TestId("agg-1"));
        repository.save(aggregate);

        var found = repository.findById(new TestId("agg-1"));
        assertThat(found).isPresent();
        assertThat(found.get().id().value()).isEqualTo("agg-1");
    }

    @Test
    void shouldReturnSavedAggregate() {
        var aggregate = new TestAggregate(new TestId("agg-1"));
        var saved = repository.save(aggregate);

        assertThat(saved).isSameAs(aggregate);
    }

    @Test
    void shouldDeleteAggregate() {
        var aggregate = new TestAggregate(new TestId("agg-1"));
        repository.save(aggregate);
        assertThat(repository.findById(new TestId("agg-1"))).isPresent();

        repository.delete(aggregate);
        assertThat(repository.findById(new TestId("agg-1"))).isEmpty();
    }

    @Test
    void shouldRejectNullId() {
        assertThatThrownBy(() -> repository.findById(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullSave() {
        assertThatThrownBy(() -> repository.save(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullDelete() {
        assertThatThrownBy(() -> repository.delete(null))
                .isInstanceOf(NullPointerException.class);
    }
}
