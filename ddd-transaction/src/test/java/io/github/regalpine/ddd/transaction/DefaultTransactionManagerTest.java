package io.github.regalpine.ddd.transaction;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static io.github.regalpine.ddd.transaction.TransactionPropagation.*;

class DefaultTransactionManagerTest {
    @Test void shouldJoinRequiredAndDeferCallbacks() {
        var adapter = new Adapter();
        var manager = new DefaultTransactionManager(adapter);
        manager.execute(() -> {
            manager.execute(() -> manager.registerSynchronization(sync(adapter.calls, false)));
            assertThat(adapter.calls).containsExactly("begin:1");
        });
        assertThat(adapter.calls).containsExactly("begin:1", "before", "commit:1", "close:1", "after");
        assertThat(manager.isActive()).isFalse();
    }

    @Test void shouldRollbackEvenWhenOuterCatchesInnerFailure() {
        var adapter = new Adapter();
        var manager = new DefaultTransactionManager(adapter);
        assertThatThrownBy(() -> manager.execute(() -> {
            try { manager.execute(() -> { throw new IllegalArgumentException("business"); }); }
            catch (IllegalArgumentException ignored) { }
        })).hasMessageContaining("rollback-only");
        assertThat(adapter.calls).containsExactly("begin:1", "rollback:1", "close:1");
    }

    @Test void shouldSuspendAndRestoreOuterTransaction() {
        var adapter = new Adapter();
        var manager = new DefaultTransactionManager(adapter);
        manager.execute(() -> {
            manager.execute(TransactionDefinition.of(REQUIRES_NEW), () -> assertThat(adapter.id).isEqualTo(2));
            manager.execute(TransactionDefinition.of(NOT_SUPPORTED), () -> {
                assertThat(manager.isActive()).isFalse();
                assertThat(adapter.isActive()).isFalse();
            });
            assertThat(adapter.id).isEqualTo(1);
        });
        assertThat(adapter.calls).containsExactly("begin:1", "begin:2", "commit:2", "close:2", "commit:1", "close:1");
    }

    @Test void shouldImplementNonTransactionalPropagation() {
        var adapter = new Adapter();
        var manager = new DefaultTransactionManager(adapter);
        for (var propagation : List.of(SUPPORTS, NEVER, NOT_SUPPORTED)) {
            manager.execute(TransactionDefinition.of(propagation), () -> assertThat(manager.isActive()).isFalse());
        }
        assertThatThrownBy(() -> manager.execute(TransactionDefinition.of(MANDATORY), () -> {}))
                .isInstanceOf(IllegalStateException.class);
        manager.execute(() -> {
            manager.execute(TransactionDefinition.of(MANDATORY), () -> assertThat(manager.isActive()).isTrue());
            manager.execute(TransactionDefinition.of(SUPPORTS), () -> assertThat(manager.isActive()).isTrue());
            assertThatThrownBy(() -> manager.execute(TransactionDefinition.of(NEVER), () -> {}))
                    .isInstanceOf(IllegalStateException.class);
        });
    }

    @Test void shouldNotRollbackCommittedWorkWhenNotificationFails() {
        var adapter = new Adapter();
        var manager = new DefaultTransactionManager(adapter);
        assertThatThrownBy(() -> manager.execute(() -> manager.registerSynchronization(sync(adapter.calls, true))))
                .isInstanceOf(TransactionCommittedException.class);
        assertThat(adapter.calls).doesNotContain("rollback:1");
        assertThat(manager.isActive()).isFalse();
    }

    @Test void shouldRollbackErrorsAndPreserveCleanupFailure() {
        var adapter = new Adapter();
        adapter.failRollback = true;
        var manager = new DefaultTransactionManager(adapter);
        var primary = new AssertionError("primary");
        assertThatThrownBy(() -> manager.execute(() -> { throw primary; }))
                .isSameAs(primary).satisfies(e -> assertThat(e.getSuppressed()).hasSize(1));
        assertThat(adapter.calls).endsWith("close:1");
    }

    @Test void shouldEnforceTimeoutAndAllowThreadReuse() {
        var adapter = new Adapter();
        var manager = new DefaultTransactionManager(adapter);
        var definition = TransactionDefinition.of(REQUIRED, TransactionIsolation.DEFAULT, Duration.ofNanos(1));
        assertThatThrownBy(() -> manager.execute(definition, () -> {})).hasMessageContaining("超时");
        manager.execute(() -> {});
        assertThat(adapter.calls).contains("rollback:1", "commit:2");
    }

    private TransactionSynchronization sync(List<String> calls, boolean fail) {
        return new TransactionSynchronization() {
            public void beforeCommit() { calls.add("before"); }
            public void afterCommit() { calls.add("after"); if (fail) throw new IllegalStateException("notify"); }
            public void afterRollback() { calls.add("rolledBack"); }
        };
    }
    private static final class Adapter implements TransactionAdapter {
        int id;
        int next;
        boolean failRollback;
        final List<String> calls = new ArrayList<>();
        public void begin(TransactionDefinition definition) { id = ++next; calls.add("begin:" + id); }
        public void commit() { calls.add("commit:" + id); }
        public void rollback() { calls.add("rollback:" + id); if (failRollback) throw new IllegalStateException("rollback"); }
        public void close() { if (id != 0) calls.add("close:" + id); id = 0; }
        public boolean isActive() { return id != 0; }
        public Object suspend() { int old = id; id = 0; return old; }
        public void resume(Object resource) { id = (Integer) resource; }
    }
}
