package io.github.regalpine.ddd.transaction;

import io.github.regalpine.ddd.core.exception.ConcurrencyException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link ConcurrencyConflictException}.
 */
class ConcurrencyConflictExceptionTest {

    @Test
    void shouldExposeAllFourFields() {
        var ex = new ConcurrencyConflictException("Order", "order-123", 7, 8);

        assertThat(ex.aggregateType()).isEqualTo("Order");
        assertThat(ex.aggregateId()).isEqualTo("order-123");
        assertThat(ex.expectedVersion()).isEqualTo(7);
        assertThat(ex.actualVersion()).isEqualTo(8);
    }

    @Test
    void shouldExtendConcurrencyException() {
        var ex = new ConcurrencyConflictException("Order", "id-1", 1, 2);

        assertThat(ex).isInstanceOf(ConcurrencyException.class);
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void messageShouldContainVersionInfo() {
        var ex = new ConcurrencyConflictException("Order", "id-1", 7, 8);

        assertThat(ex.getMessage()).contains("7").contains("8");
    }
}
