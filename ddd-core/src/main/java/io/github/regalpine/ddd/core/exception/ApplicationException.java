package io.github.regalpine.ddd.core.exception;

/**
 * Exception for application-layer errors.
 *
 * <p>Thrown by application services when a use case cannot be completed
 * due to business-level (non-domain) constraints.</p>
 *
 * @author RegalPine
 */
public class ApplicationException extends DddException {

    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
