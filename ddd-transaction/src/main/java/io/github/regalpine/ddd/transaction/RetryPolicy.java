package io.github.regalpine.ddd.transaction;

import java.time.Duration;
import java.util.Objects;

/**
 * Defines the retry policy for transient failures such as concurrency conflicts.
 * <p>
 * Concurrency conflicts are potential transient failures and may be retried,
 * but retry must be bounded (no infinite retry).
 *
 * @param maxAttempts the maximum number of attempts (must be &gt; 0)
 * @param backoff     the duration to wait between retries
 * @author RegalPine
 */
public record RetryPolicy(int maxAttempts, Duration backoff) {

    /**
     * Default retry policy: 3 attempts with 100ms backoff.
     */
    public static final RetryPolicy DEFAULT = new RetryPolicy(3, Duration.ofMillis(100));

    public RetryPolicy {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts must be positive, was: " + maxAttempts);
        }
        Objects.requireNonNull(backoff, "backoff must not be null");
    }
}
