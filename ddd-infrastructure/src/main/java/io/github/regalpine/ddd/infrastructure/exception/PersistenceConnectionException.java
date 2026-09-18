package io.github.regalpine.ddd.infrastructure.exception;

/**
 * Thrown when a database connection cannot be obtained or is lost.
 *
 * <p>Typical causes: connection pool exhaustion, network failure,
 * or database server unavailability.</p>
 *
 * @author RegalPine
 */
public class PersistenceConnectionException extends PersistenceException {

    public PersistenceConnectionException(String message) {
        super(message);
    }

    public PersistenceConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
