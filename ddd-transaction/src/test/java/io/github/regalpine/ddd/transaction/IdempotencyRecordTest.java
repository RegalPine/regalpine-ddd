package io.github.regalpine.ddd.transaction;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link IdempotencyRecord} and {@link IdempotencyStatus}.
 */
class IdempotencyRecordTest {

    @Test
    void shouldExposeAllFields() {
        var record = new IdempotencyRecord("cmd-1", "CreateOrder", IdempotencyStatus.COMPLETED, "order-42");

        assertThat(record.commandId()).isEqualTo("cmd-1");
        assertThat(record.commandType()).isEqualTo("CreateOrder");
        assertThat(record.status()).isEqualTo(IdempotencyStatus.COMPLETED);
        assertThat(record.resultReference()).isEqualTo("order-42");
    }

    @Test
    void statusEnumShouldHaveThreeValues() {
        assertThat(IdempotencyStatus.values()).containsExactly(
                IdempotencyStatus.PROCESSING,
                IdempotencyStatus.COMPLETED,
                IdempotencyStatus.FAILED
        );
    }

    @Test
    void recordEqualityShouldWork() {
        var r1 = new IdempotencyRecord("cmd-1", "Pay", IdempotencyStatus.PROCESSING, null);
        var r2 = new IdempotencyRecord("cmd-1", "Pay", IdempotencyStatus.PROCESSING, null);

        assertThat(r1).isEqualTo(r2);
    }
}
