package io.github.regalpine.ddd.transaction;

import io.github.regalpine.ddd.core.aggregate.AggregateRoot;
import io.github.regalpine.ddd.core.event.DomainEvent;
import io.github.regalpine.ddd.core.identifier.Identifier;
import io.github.regalpine.ddd.core.version.Version;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link UnitOfWork} interface contract using a simple in-test implementation.
 */
class UnitOfWorkTest {

    record TestId(String value) implements Identifier {}

    static final class TestAggregate implements AggregateRoot<TestId> {
        private final TestId id;
        TestAggregate(String id) { this.id = new TestId(id); }
        @Override public TestId id() { return id; }
        @Override public Version version() { return new Version(1); }
        @Override public List<DomainEvent> domainEvents() { return List.of(); }
        @Override public void clearDomainEvents() {}
    }

    /** Simple UoW implementation for testing. */
    static final class SimpleUnitOfWork implements UnitOfWork {
        private final String id;
        private UnitOfWorkStatus status = UnitOfWorkStatus.NEW;
        private final java.util.List<Object> tracked = new java.util.ArrayList<>();

        SimpleUnitOfWork(String id) { this.id = id; }
        @Override public String id() { return id; }
        @Override public UnitOfWorkStatus status() { return status; }
        @Override public <A extends AggregateRoot<I>, I extends Identifier> void register(A aggregate) {
            if (status != UnitOfWorkStatus.NEW && status != UnitOfWorkStatus.ACTIVE)
                throw new IllegalStateException("Cannot register in status: " + status);
            status = UnitOfWorkStatus.ACTIVE;
            tracked.add(aggregate);
        }
        @Override public boolean contains(Object aggregate) { return tracked.contains(aggregate); }
        @Override public List<AggregateRoot<?>> trackedAggregates() {
            @SuppressWarnings("unchecked")
            List<AggregateRoot<?>> result = (List<AggregateRoot<?>>) (List<?>) tracked;
            return List.copyOf(result);
        }
        @Override public void commit() {
            if (status != UnitOfWorkStatus.ACTIVE) throw new IllegalStateException("Cannot commit in status: " + status);
            status = UnitOfWorkStatus.COMMITTING;
            status = UnitOfWorkStatus.COMMITTED;
        }
        @Override public void rollback() {
            if (status == UnitOfWorkStatus.COMMITTED || status == UnitOfWorkStatus.ROLLED_BACK || status == UnitOfWorkStatus.CLOSED)
                throw new IllegalStateException("Cannot rollback in status: " + status);
            status = UnitOfWorkStatus.ROLLING_BACK;
            tracked.clear();
            status = UnitOfWorkStatus.ROLLED_BACK;
        }
        @Override public boolean isActive() { return status == UnitOfWorkStatus.NEW || status == UnitOfWorkStatus.ACTIVE; }
    }

    private SimpleUnitOfWork uow;

    @BeforeEach
    void setUp() { uow = new SimpleUnitOfWork("uow-1"); }

    @Test
    void registerShouldTrackAggregateAndActivateUoW() {
        var agg = new TestAggregate("a1");
        uow.register(agg);

        assertThat(uow.contains(agg)).isTrue();
        assertThat(uow.status()).isEqualTo(UnitOfWorkStatus.ACTIVE);
        assertThat(uow.trackedAggregates()).hasSize(1);
    }

    @Test
    void commitShouldTransitionThroughCommittingToCommitted() {
        uow.register(new TestAggregate("a1"));
        uow.commit();

        assertThat(uow.status()).isEqualTo(UnitOfWorkStatus.COMMITTED);
        assertThat(uow.isActive()).isFalse();
    }

    @Test
    void rollbackShouldClearTrackedAndTransitionToRolledBack() {
        var agg = new TestAggregate("a1");
        uow.register(agg);
        uow.rollback();

        assertThat(uow.status()).isEqualTo(UnitOfWorkStatus.ROLLED_BACK);
        assertThat(uow.trackedAggregates()).isEmpty();
    }

    @Test
    void commitAfterCommitShouldFail() {
        uow.register(new TestAggregate("a1"));
        uow.commit();

        assertThatThrownBy(uow::commit).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void isActiveShouldReturnTrueOnlyForNewAndActive() {
        assertThat(uow.isActive()).isTrue(); // NEW
        uow.register(new TestAggregate("a1")); // -> ACTIVE
        assertThat(uow.isActive()).isTrue();
        uow.commit(); // -> COMMITTED
        assertThat(uow.isActive()).isFalse();
    }
}
