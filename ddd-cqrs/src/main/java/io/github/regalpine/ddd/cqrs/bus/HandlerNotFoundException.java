package io.github.regalpine.ddd.cqrs.bus;

import io.github.regalpine.ddd.core.exception.ApplicationException;

/**
 * Thrown when no handler is registered for a given command or query type.
 *
 * @author RegalPine
 */
public class HandlerNotFoundException extends ApplicationException {

    public HandlerNotFoundException(String messageType) {
        super("No handler registered for: " + messageType);
    }
}
