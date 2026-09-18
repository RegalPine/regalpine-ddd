package io.github.regalpine.ddd.infrastructure.exception;

import io.github.regalpine.ddd.core.exception.DddException;

/**
 * Exception for infrastructure-layer errors.
 *
 * <p>Thrown when infrastructure components (persistence, messaging,
 * external services) encounter failures.</p>
 *
 * @author RegalPine
 */
public class InfrastructureException extends DddException {

    public InfrastructureException(String message) {
        super(message);
    }

    public InfrastructureException(String message, Throwable cause) {
        super(message, cause);
    }
}
