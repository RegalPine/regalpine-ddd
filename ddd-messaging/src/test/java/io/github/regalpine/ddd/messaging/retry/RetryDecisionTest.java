package io.github.regalpine.ddd.messaging.retry;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

class RetryDecisionTest {

    @Test
    void retryShouldCreateRetryDecision() {
        RetryDecision decision = RetryDecision.retry(Duration.ofMillis(500));

        assertThat(decision.shouldRetry()).isTrue();
        assertThat(decision.delay()).isEqualTo(Duration.ofMillis(500));
    }

    @Test
    void noRetryShouldCreateStopDecision() {
        RetryDecision decision = RetryDecision.noRetry();

        assertThat(decision.shouldRetry()).isFalse();
        assertThat(decision.delay()).isEqualTo(Duration.ZERO);
    }
}
