package io.github.regalpine.ddd.messaging.retry;

import java.time.Duration;
import java.util.Objects;

/**
 * Defines the retry decision for a failed message processing attempt.
 *
 * <p>Phase IX §33: Retry supports Fixed, Exponential, and Exponential + Jitter strategies.
 * Exponential + Jitter is recommended as default.</p>
 *
 * @param shouldRetry whether to retry
 * @param delay       the delay before the next retry attempt
 * @author RegalPine
 */
public record RetryDecision(boolean shouldRetry, Duration delay) {

    public RetryDecision {
        Objects.requireNonNull(delay, "delay must not be null");
    }

    /**
     * Creates a decision to retry with the given delay.
     */
    public static RetryDecision retry(Duration delay) {
        return new RetryDecision(true, delay);
    }

    /**
     * Creates a decision to not retry (send to DLQ).
     */
    public static RetryDecision noRetry() {
        return new RetryDecision(false, Duration.ZERO);
    }
}
