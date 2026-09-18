package io.github.regalpine.ddd.transaction;

import io.github.regalpine.ddd.core.exception.ConcurrencyException;

/**
 * Thrown when an optimistic concurrency conflict is detected.
 * <p>
 * This occurs when an aggregate's version has been modified by another
 * transaction since it was loaded. This exception belongs to the
 * Application / Infrastructure layer, not to Domain Business Errors.
 *
 * @author RegalPine
 */
public class ConcurrencyConflictException extends ConcurrencyException {

    private final String aggregateType;
    private final String aggregateId;
    private final long expectedVersion;
    private final long actualVersion;

    public ConcurrencyConflictException(
            String aggregateType,
            String aggregateId,
            long expectedVersion,
            long actualVersion) {
        super("Concurrency conflict for %s [%s]: expected version %d but was %d"
                .formatted(aggregateType, aggregateId, expectedVersion, actualVersion));
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.expectedVersion = expectedVersion;
        this.actualVersion = actualVersion;
    }

    public String aggregateType() {
        return aggregateType;
    }

    public String aggregateId() {
        return aggregateId;
    }

    public long expectedVersion() {
        return expectedVersion;
    }

    public long actualVersion() {
        return actualVersion;
    }
}
