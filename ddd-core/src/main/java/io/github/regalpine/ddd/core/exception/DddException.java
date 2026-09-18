package io.github.regalpine.ddd.core.exception;

/**
 * Base exception for all framework-level errors.
 *
 * <p>The exception hierarchy is:</p>
 * <pre>{@code
 * FrameworkException (Phase XI §111)
 *  └── DddException (backward-compatible base)
 *       ├── DomainException
 *       ├── ApplicationException
 *       ├── InfrastructureException
 *       ├── ValidationException
 *       ├── AuthorizationException
 *       ├── ConcurrencyException
 *       └── IdempotencyException
 * }</pre>
 *
 * <p>All exceptions are unchecked ({@link RuntimeException}) to avoid
 * forcing checked exceptions through the domain model.</p>
 *
 * @author RegalPine
 */
public class DddException extends FrameworkException {

    public DddException(String message) {
        super(message);
    }

    public DddException(String message, Throwable cause) {
        super(message, cause);
    }
}
