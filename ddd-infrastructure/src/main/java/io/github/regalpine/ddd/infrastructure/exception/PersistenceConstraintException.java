package io.github.regalpine.ddd.infrastructure.exception;

import java.util.Objects;

/**
 * Thrown when a database constraint is violated.
 *
 * <p>Covers UNIQUE, FOREIGN KEY, CHECK, and NOT NULL violations.
 * The {@link ConstraintType} field preserves enough semantics for
 * the Application layer to make informed decisions (Phase VII §63).</p>
 *
 * @author RegalPine
 */
public class PersistenceConstraintException extends PersistenceException {

    /**
     * The type of database constraint that was violated.
     */
    public enum ConstraintType {
        /** UNIQUE constraint violation. */
        UNIQUE,
        /** FOREIGN KEY constraint violation. */
        FK,
        /** CHECK constraint violation. */
        CHECK,
        /** NOT NULL constraint violation. */
        NOT_NULL
    }

    private final ConstraintType constraintType;

    /**
     * Creates a new constraint violation exception.
     *
     * @param message        the detail message
     * @param constraintType the type of constraint violated
     */
    public PersistenceConstraintException(String message, ConstraintType constraintType) {
        super(message);
        this.constraintType = Objects.requireNonNull(constraintType, "constraintType must not be null");
    }

    /**
     * Creates a new constraint violation exception with a cause.
     *
     * @param message        the detail message
     * @param constraintType the type of constraint violated
     * @param cause          the underlying cause
     */
    public PersistenceConstraintException(String message, ConstraintType constraintType, Throwable cause) {
        super(message, cause);
        this.constraintType = Objects.requireNonNull(constraintType, "constraintType must not be null");
    }

    /**
     * Returns the type of constraint that was violated.
     *
     * @return the constraint type, never {@code null}
     */
    public ConstraintType constraintType() {
        return constraintType;
    }
}
