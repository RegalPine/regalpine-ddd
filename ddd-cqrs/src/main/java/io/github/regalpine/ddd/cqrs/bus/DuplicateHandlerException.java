package io.github.regalpine.ddd.cqrs.bus;

import io.github.regalpine.ddd.core.exception.ApplicationException;

/**
 * Thrown when multiple handlers are registered for the same command or query type.
 * <p>
 * The framework enforces One Command/Query Type → One Handler uniqueness.
 *
 * @author RegalPine
 */
public class DuplicateHandlerException extends ApplicationException {

    public DuplicateHandlerException(String messageType) {
        super("Duplicate handler registered for: " + messageType);
    }
}
