package io.github.regalpine.ddd.core.exception;

/**
 * Exception for optimistic concurrency conflicts.
 *
 * <p>Thrown when an aggregate's version indicates that another
 * transaction has modified the same data concurrently.</p>
 *
 * @author RegalPine
 */
public class ConcurrencyException extends DddException {

    public ConcurrencyException(String message) {
        super(message);
    }

    public ConcurrencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
