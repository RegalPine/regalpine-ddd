package io.github.regalpine.ddd.messaging.retry;

/**
 * Determines the retry strategy for failed message processing.
 *
 * <p>Phase IX §33: RetryPolicy computes the next retry decision based on
 * the current attempt number and the error that occurred.</p>
 *
 * <p>Phase IX §35/§36: Non-retryable failures (ValidationException, AuthorizationException,
 * BusinessRuleViolation, etc.) must not be retried infinitely. Retry must have a finite
 * upper bound (maxAttempts) (MSG-015).</p>
 *
 * <p>Note: This is distinct from {@code io.github.regalpine.ddd.transaction.RetryPolicy}
 * which handles transaction-level retries.</p>
 *
 * @author RegalPine
 */
public interface RetryPolicy {

    /**
     * Determines the next retry decision.
     *
     * @param attempt the current attempt number (1-based)
     * @param error   the error that caused the failure
     * @return the retry decision
     */
    RetryDecision next(int attempt, Throwable error);
}
