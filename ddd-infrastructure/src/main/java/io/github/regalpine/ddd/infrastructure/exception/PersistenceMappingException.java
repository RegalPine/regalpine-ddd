package io.github.regalpine.ddd.infrastructure.exception;

/**
 * Thrown when domain-to-persistence or persistence-to-domain mapping fails.
 *
 * <p>Typical causes: missing required fields in the persistence record,
 * type conversion errors, or structural mismatches between domain and
 * persistence models.</p>
 *
 * @author RegalPine
 */
public class PersistenceMappingException extends PersistenceException {

    public PersistenceMappingException(String message) {
        super(message);
    }

    public PersistenceMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
