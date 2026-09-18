package io.github.regalpine.ddd.runtime;

/**
 * Result of an outbox dispatch batch operation.
 *
 * @param dispatched the number of records successfully published
 * @param failed     the number of records that failed to publish
 * @param remaining  the number of pending records remaining
 * @author RegalPine
 */
public record DispatchResult(int dispatched, int failed, int remaining) {

    public DispatchResult {
        if (dispatched < 0) throw new IllegalArgumentException("dispatched must not be negative");
        if (failed < 0) throw new IllegalArgumentException("failed must not be negative");
        if (remaining < 0) throw new IllegalArgumentException("remaining must not be negative");
    }
}
