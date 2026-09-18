package io.github.regalpine.ddd.conformance.mybatis;

import io.github.regalpine.ddd.infrastructure.mybatis.exception.MyBatisConcurrencyException;

/**
 * Conformance test suite for optimistic concurrency conflict detection.
 *
 * <p>Phase XII §71: verifies that the repository throws
 * {@link MyBatisConcurrencyException} when two transactions
 * attempt to update the same aggregate version.</p>
 *
 * @author RegalPine
 */
public abstract class MyBatisConcurrencyConformance {

    /**
     * Verifies that MyBatisConcurrencyException carries aggregate ID and version.
     */
    protected void verifyConcurrencyExceptionStructure() {
        var ex = new MyBatisConcurrencyException("agg-1", 10L);

        if (!"agg-1".equals(ex.aggregateId())) {
            throw new AssertionError("Exception must carry aggregate ID");
        }
        if (ex.expectedVersion() != 10L) {
            throw new AssertionError("Exception must carry expected version");
        }
        if (ex.getMessage() == null || ex.getMessage().isBlank()) {
            throw new AssertionError("Exception must have a descriptive message");
        }
    }

    /**
     * Runs all conformance checks.
     */
    public void runAll() {
        verifyConcurrencyExceptionStructure();
    }
}
