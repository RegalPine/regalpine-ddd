package io.github.regalpine.ddd.core.exception;

/**
 * Exception for authorization failures.
 *
 * <p>Thrown when the current principal lacks permission
 * to perform the requested operation.</p>
 *
 * @author RegalPine
 */
public class AuthorizationException extends DddException {

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
