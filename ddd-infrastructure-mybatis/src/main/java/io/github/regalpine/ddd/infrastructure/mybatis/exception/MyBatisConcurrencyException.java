package io.github.regalpine.ddd.infrastructure.mybatis.exception;

/**
 * Thrown when an optimistic concurrency conflict is detected during
 * a MyBatis repository update operation.
 *
 * <p>Phase XII §11: when {@code UPDATE ... WHERE version = #{expectedVersion}}
 * affects zero rows, a concurrency conflict has occurred.</p>
 *
 * @author RegalPine
 */
public class MyBatisConcurrencyException extends MyBatisAdapterException {

    private final String aggregateId;
    private final long expectedVersion;

    public MyBatisConcurrencyException(String aggregateId, long expectedVersion) {
        super("Concurrency conflict on aggregate [" + aggregateId
                + "]: expected version " + expectedVersion);
        this.aggregateId = aggregateId;
        this.expectedVersion = expectedVersion;
    }

    /**
     * Returns the aggregate identifier that had the conflict.
     */
    public String aggregateId() {
        return aggregateId;
    }

    /**
     * Returns the expected version that did not match.
     */
    public long expectedVersion() {
        return expectedVersion;
    }
}
