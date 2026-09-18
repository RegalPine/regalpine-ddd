package io.github.regalpine.ddd.infrastructure.exception;

/**
 * Base exception for persistence-layer errors.
 *
 * <p>Per Phase VII §62, database exceptions must not leak to the Application layer.
 * Infrastructure must map {@code SQLException} and similar to a persistence
 * exception hierarchy rooted at {@code PersistenceException}.</p>
 *
 * <p>The hierarchy is:</p>
 * <pre>{@code
 * PersistenceException
 *  ├── PersistenceAccessException
 *  ├── PersistenceMappingException
 *  ├── PersistenceConstraintException
 *  ├── PersistenceConnectionException
 *  └── PersistenceTimeoutException
 * }</pre>
 *
 * @author RegalPine
 */
public class PersistenceException extends InfrastructureException {

    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
