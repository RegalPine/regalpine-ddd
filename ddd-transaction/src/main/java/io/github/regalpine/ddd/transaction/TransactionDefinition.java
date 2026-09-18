package io.github.regalpine.ddd.transaction;

import java.time.Duration;
import java.util.Objects;

/**
 * Defines the parameters for a transaction.
 *
 * <p>Phase XI §46: includes propagation, isolation, readOnly flag, and timeout.</p>
 *
 * @author RegalPine
 */
public record TransactionDefinition(
        TransactionPropagation propagation,
        TransactionIsolation isolation,
        boolean readOnly,
        Duration timeout
) {

    public static final TransactionDefinition DEFAULT = required();

    public TransactionDefinition {
        Objects.requireNonNull(propagation, "propagation must not be null");
        Objects.requireNonNull(isolation, "isolation must not be null");
        Objects.requireNonNull(timeout, "timeout must not be null");
        if (timeout.isNegative()) {
            throw new IllegalArgumentException("timeout 不能为负数");
        }
    }

    /**
     * Creates a definition suitable for command handling: REQUIRED propagation,
     * DEFAULT isolation, read-write, no timeout.
     */
    public static TransactionDefinition required() {
        return new TransactionDefinition(
                TransactionPropagation.REQUIRED,
                TransactionIsolation.DEFAULT,
                false,
                Duration.ZERO
        );
    }

    /**
     * Creates a definition suitable for query handling: SUPPORTS propagation,
     * DEFAULT isolation, read-only, no timeout.
     */
    public static TransactionDefinition readOnlyDefinition() {
        return new TransactionDefinition(
                TransactionPropagation.SUPPORTS,
                TransactionIsolation.DEFAULT,
                true,
                Duration.ZERO
        );
    }

    public static TransactionDefinition of(TransactionPropagation propagation) {
        return new TransactionDefinition(propagation, TransactionIsolation.DEFAULT, false, Duration.ZERO);
    }

    public static TransactionDefinition of(TransactionPropagation propagation, TransactionIsolation isolation) {
        return new TransactionDefinition(propagation, isolation, false, Duration.ZERO);
    }

    public static TransactionDefinition of(TransactionPropagation propagation, TransactionIsolation isolation, Duration timeout) {
        return new TransactionDefinition(propagation, isolation, false, timeout);
    }
}
