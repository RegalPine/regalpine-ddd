package io.github.regalpine.ddd.core.exception;

import io.github.regalpine.ddd.core.error.DomainError;

import java.util.Objects;

/**
 * Base exception for all domain-level errors.
 *
 * <p>DomainException carries a {@link DomainError} that provides a stable
 * error code and message. It extends {@link DddException} as part of the
 * framework exception hierarchy.</p>
 *
 * <p>Subclasses may represent specific domain error categories such as
 * validation failures, invariant violations, or state transition errors.</p>
 *
 * @author RegalPine
 */
public class DomainException extends DddException {

    private final DomainError error;

    /**
     * Creates a new DomainException with the given domain error.
     *
     * @param error the domain error, must not be {@code null}
     */
    public DomainException(DomainError error) {
        super(Objects.requireNonNull(error, "Domain error must not be null").message());
        this.error = error;
    }

    /**
     * Creates a new DomainException with the given domain error and cause.
     *
     * @param error the domain error, must not be {@code null}
     * @param cause the underlying cause
     */
    public DomainException(DomainError error, Throwable cause) {
        super(Objects.requireNonNull(error, "Domain error must not be null").message(), cause);
        this.error = error;
    }

    /**
     * Returns the domain error associated with this exception.
     *
     * @return the domain error, never {@code null}
     */
    public DomainError error() {
        return error;
    }
}
