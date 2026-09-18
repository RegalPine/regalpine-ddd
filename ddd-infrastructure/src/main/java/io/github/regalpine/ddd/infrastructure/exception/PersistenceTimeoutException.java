package io.github.regalpine.ddd.infrastructure.exception;

/**
 * Thrown when a persistence operation exceeds its time limit.
 *
 * <p>Typical causes: transaction timeout, query timeout, or
 * lock-wait timeout.</p>
 *
 * @author RegalPine
 */
public class PersistenceTimeoutException extends PersistenceException {

    public PersistenceTimeoutException(String message) {
        super(message);
    }

    public PersistenceTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
