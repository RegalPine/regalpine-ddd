package io.github.regalpine.ddd.transaction;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link UnitOfWorkManager} interface contract.
 */
class UnitOfWorkManagerTest {

    /** Minimal in-test UnitOfWorkManager. */
    static final class SimpleManager implements UnitOfWorkManager {
        private final ThreadLocal<UnitOfWork> current = new ThreadLocal<>();
        private int counter = 0;

        @Override
        public UnitOfWork begin() {
            var uow = new UnitOfWork() {
                private final String id = "uow-" + (++counter);
                private UnitOfWorkStatus status = UnitOfWorkStatus.NEW;
                @Override public String id() { return id; }
                @Override public UnitOfWorkStatus status() { return status; }
                @Override public <A extends io.github.regalpine.ddd.core.aggregate.AggregateRoot<I>, I extends io.github.regalpine.ddd.core.identifier.Identifier> void register(A aggregate) { status = UnitOfWorkStatus.ACTIVE; }
                @Override public boolean contains(Object aggregate) { return false; }
                @Override public java.util.List<io.github.regalpine.ddd.core.aggregate.AggregateRoot<?>> trackedAggregates() { return java.util.List.of(); }
                @Override public void commit() { status = UnitOfWorkStatus.COMMITTED; }
                @Override public void rollback() { status = UnitOfWorkStatus.ROLLED_BACK; }
                @Override public boolean isActive() { return status == UnitOfWorkStatus.NEW || status == UnitOfWorkStatus.ACTIVE; }
            };
            current.set(uow);
            return uow;
        }
        @Override public UnitOfWork current() { return current.get(); }
        @Override public void complete() { current.remove(); }
        @Override public void rollback() {
            UnitOfWork uow = current.get();
            if (uow != null) { uow.rollback(); current.remove(); }
        }
        @Override public boolean hasCurrent() {
            UnitOfWork uow = current.get();
            return uow != null && uow.isActive();
        }
    }

    @Test
    void beginShouldCreateAndSetCurrentUoW() {
        var mgr = new SimpleManager();
        UnitOfWork uow = mgr.begin();

        assertThat(uow).isNotNull();
        assertThat(mgr.current()).isSameAs(uow);
        assertThat(mgr.hasCurrent()).isTrue();
    }

    @Test
    void completeShouldRemoveCurrentUoW() {
        var mgr = new SimpleManager();
        mgr.begin();
        mgr.complete();

        assertThat(mgr.current()).isNull();
        assertThat(mgr.hasCurrent()).isFalse();
    }

    @Test
    void rollbackShouldRollbackAndRemoveUoW() {
        var mgr = new SimpleManager();
        UnitOfWork uow = mgr.begin();
        mgr.rollback();

        assertThat(uow.status()).isEqualTo(UnitOfWorkStatus.ROLLED_BACK);
        assertThat(mgr.hasCurrent()).isFalse();
    }

    @Test
    void hasCurrentShouldReturnFalseWhenNoUoW() {
        var mgr = new SimpleManager();
        assertThat(mgr.hasCurrent()).isFalse();
    }
}
