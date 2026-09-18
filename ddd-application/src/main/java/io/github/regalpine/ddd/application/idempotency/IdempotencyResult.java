package io.github.regalpine.ddd.application.idempotency;

/**
 * Represents the result of a previously executed command.
 *
 * <p>Returned by {@link IdempotencyStore#find(String)} to indicate
 * whether a command has already been processed and, if so, what
 * the original result was.</p>
 *
 * @param alreadyProcessed whether the command was already handled
 * @param previousResult   the result from the original execution, may be {@code null}
 */
public record IdempotencyResult(
        boolean alreadyProcessed,
        Object previousResult
) {

    /**
     * Creates a result indicating the command was already processed.
     *
     * @param previousResult the result from the original execution
     * @return an idempotency result indicating duplicate processing
     */
    public static IdempotencyResult alreadyProcessed(Object previousResult) {
        return new IdempotencyResult(true, previousResult);
    }

    /**
     * Creates a result indicating the command has not been processed yet.
     *
     * @return an idempotency result indicating no prior execution
     */
    public static IdempotencyResult notProcessed() {
        return new IdempotencyResult(false, null);
    }
}
