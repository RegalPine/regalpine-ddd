package io.github.regalpine.ddd.core.exception;

/**
 * Exception for idempotency violations.
 *
 * <p>Thrown when a command is detected as a duplicate of one
 * that has already been processed.</p>
 *
 * @author RegalPine
 */
public class IdempotencyException extends DddException {

    public IdempotencyException(String message) {
        super(message);
    }

    public IdempotencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
