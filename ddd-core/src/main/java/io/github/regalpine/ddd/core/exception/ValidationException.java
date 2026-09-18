package io.github.regalpine.ddd.core.exception;

/**
 * Exception for input validation failures.
 *
 * <p>Thrown when command or query inputs fail validation checks
 * before reaching the domain layer.</p>
 *
 * @author RegalPine
 */
public class ValidationException extends DddException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
