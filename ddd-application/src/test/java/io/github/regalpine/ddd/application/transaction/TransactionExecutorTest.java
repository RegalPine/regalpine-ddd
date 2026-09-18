package io.github.regalpine.ddd.application.transaction;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionExecutorTest {

    // -- Test fixtures --

    /**
     * Simple in-memory transaction executor for testing.
     */
    static final class InMemoryTransactionExecutor implements TransactionExecutor {
        @Override
        public <R> R execute(TransactionCallback<R> callback) {
            return callback.execute();
        }

        @Override
        public void execute(TransactionRunnable runnable) {
            runnable.run();
        }
    }

    // -- Tests --

    @Test
    void shouldExecuteCallbackAndReturnResult() {
        TransactionExecutor executor = new InMemoryTransactionExecutor();
        String result = executor.execute((TransactionCallback<String>) () -> "done");

        assertThat(result).isEqualTo("done");
    }

    @Test
    void shouldExecuteRunnable() {
        TransactionExecutor executor = new InMemoryTransactionExecutor();
        final boolean[] executed = {false};

        executor.execute((TransactionRunnable) () -> executed[0] = true);

        assertThat(executed[0]).isTrue();
    }

    @Test
    void shouldPropagateCallbackException() {
        TransactionExecutor executor = new InMemoryTransactionExecutor();

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> executor.execute((TransactionCallback<String>) () -> {
                    throw new IllegalStateException("tx failed");
                })
        ).isInstanceOf(IllegalStateException.class)
         .hasMessage("tx failed");
    }
}
