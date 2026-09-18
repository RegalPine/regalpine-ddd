package io.github.regalpine.ddd.transaction;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link TransactionSynchronization} interface contract.
 */
class TransactionSynchronizationTest {

    @Test
    void shouldInvokeAllCallbacks() {
        List<String> events = new ArrayList<>();

        TransactionSynchronization sync = new TransactionSynchronization() {
            @Override public void beforeCommit() { events.add("before-commit"); }
            @Override public void afterCommit() { events.add("after-commit"); }
            @Override public void afterRollback() { events.add("after-rollback"); }
        };

        sync.beforeCommit();
        sync.afterCommit();
        sync.afterRollback();

        assertThat(events).containsExactly("before-commit", "after-commit", "after-rollback");
    }

    @Test
    void shouldBeImplementableAsLambda() {
        TransactionSynchronization sync = new TransactionSynchronization() {
            @Override public void beforeCommit() {}
            @Override public void afterCommit() {}
            @Override public void afterRollback() {}
        };

        // Verify the interface can be implemented without error
        assertThat(sync).isNotNull();
        assertThatNoException().isThrownBy(() -> {
            sync.beforeCommit();
            sync.afterCommit();
            sync.afterRollback();
        });
    }
}
