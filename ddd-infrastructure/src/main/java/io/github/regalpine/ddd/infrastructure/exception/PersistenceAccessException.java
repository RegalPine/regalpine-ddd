package io.github.regalpine.ddd.infrastructure.exception;

/**
 * Thrown when a persistence access operation fails.
 *
 * <p>Typical causes: query execution errors, update/insert failures,
 * or result-set processing errors that are not related to connection
 * or constraint issues.</p>
 *
 * @author RegalPine
 */
public class PersistenceAccessException extends PersistenceException {

    public PersistenceAccessException(String message) {
        super(message);
    }

    public PersistenceAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
