package io.github.regalpine.ddd.transaction;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link TransactionManager}, {@link TransactionCallback}, and {@link TransactionRunnable}.
 */
class TransactionManagerTest {

    /** Minimal test adapter. */
    static final class TestAdapter implements TransactionAdapter {
        boolean active;
        int beginCount, commitCount, rollbackCount;

        @Override public void begin(TransactionDefinition definition) { active = true; beginCount++; }
        @Override public void commit() { active = false; commitCount++; }
        @Override public void rollback() { active = false; rollbackCount++; }
        @Override public boolean isActive() { return active; }
    }

    /** Minimal test TransactionManager backed by TestAdapter. */
    static final class TestTransactionManager implements TransactionManager {
        final TestAdapter adapter = new TestAdapter();

        @Override
        public <T> T execute(TransactionCallback<T> callback) {
            return execute(TransactionDefinition.DEFAULT, callback);
        }

        @Override
        public <T> T execute(TransactionDefinition definition, TransactionCallback<T> callback) {
            adapter.begin(definition);
            try {
                T result = callback.execute();
                adapter.commit();
                return result;
            } catch (Exception e) {
                adapter.rollback();
                throw e;
            }
        }

        @Override
        public void execute(TransactionRunnable runnable) {
            execute(TransactionDefinition.DEFAULT, runnable);
        }

        @Override
        public void execute(TransactionDefinition definition, TransactionRunnable runnable) {
            adapter.begin(definition);
            try {
                runnable.run();
                adapter.commit();
            } catch (Exception e) {
                adapter.rollback();
                throw e;
            }
        }
    }

    @Test
    void executeWithCallbackShouldReturnResultAndCommit() {
        var tm = new TestTransactionManager();
        String result = tm.execute((TransactionCallback<String>) () -> "done");

        assertThat(result).isEqualTo("done");
        assertThat(tm.adapter.commitCount).isEqualTo(1);
        assertThat(tm.adapter.rollbackCount).isZero();
    }

    @Test
    void executeWithRunnableShouldCommitOnSuccess() {
        var tm = new TestTransactionManager();
        boolean[] executed = {false};
        tm.execute((TransactionRunnable) () -> executed[0] = true);

        assertThat(executed[0]).isTrue();
        assertThat(tm.adapter.commitCount).isEqualTo(1);
    }

    @Test
    void executeShouldRollbackOnException() {
        var tm = new TestTransactionManager();

        assertThatThrownBy(() ->
            tm.execute((TransactionCallback<String>) () -> { throw new RuntimeException("fail"); })
        ).isInstanceOf(RuntimeException.class).hasMessage("fail");

        assertThat(tm.adapter.rollbackCount).isEqualTo(1);
        assertThat(tm.adapter.commitCount).isZero();
    }
}
