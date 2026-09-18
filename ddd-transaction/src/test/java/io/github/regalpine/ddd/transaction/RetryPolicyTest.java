package io.github.regalpine.ddd.transaction;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link RetryPolicy} record.
 */
class RetryPolicyTest {

    @Test
    void shouldExposeFields() {
        var policy = new RetryPolicy(5, Duration.ofMillis(200));

        assertThat(policy.maxAttempts()).isEqualTo(5);
        assertThat(policy.backoff()).isEqualTo(Duration.ofMillis(200));
    }

    @Test
    void defaultShouldHaveThreeAttempts() {
        assertThat(RetryPolicy.DEFAULT.maxAttempts()).isEqualTo(3);
        assertThat(RetryPolicy.DEFAULT.backoff()).isEqualTo(Duration.ofMillis(100));
    }

    @Test
    void shouldRejectInvalidMaxAttempts() {
        assertThatThrownBy(() -> new RetryPolicy(0, Duration.ofMillis(100)))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new RetryPolicy(-1, Duration.ofMillis(100)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
