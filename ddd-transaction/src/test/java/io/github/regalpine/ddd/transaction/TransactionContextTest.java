package io.github.regalpine.ddd.transaction;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link TransactionContext} interface contract.
 */
class TransactionContextTest {

    @Test
    void shouldExposeActiveReadOnlyAndIsolation() {
        TransactionContext ctx = new TransactionContext() {
            @Override public boolean active() { return true; }
            @Override public boolean readOnly() { return false; }
            @Override public TransactionIsolation isolation() { return TransactionIsolation.READ_COMMITTED; }
        };

        assertThat(ctx.active()).isTrue();
        assertThat(ctx.readOnly()).isFalse();
        assertThat(ctx.isolation()).isEqualTo(TransactionIsolation.READ_COMMITTED);
    }

    @Test
    void readOnlyContextShouldReportReadOnly() {
        TransactionContext ctx = new TransactionContext() {
            @Override public boolean active() { return true; }
            @Override public boolean readOnly() { return true; }
            @Override public TransactionIsolation isolation() { return TransactionIsolation.DEFAULT; }
        };

        assertThat(ctx.readOnly()).isTrue();
    }
}
