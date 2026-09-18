package io.github.regalpine.ddd.runtime.exception;

/**
 * Thrown when a runtime component encounters an error.
 *
 * <p>Typical causes: component startup failure, component stop failure,
 * or component execution error.</p>
 *
 * @author RegalPine
 */
public class ComponentException extends DddRuntimeException {

    public ComponentException(String message) {
        super(message);
    }

    public ComponentException(String message, Throwable cause) {
        super(message, cause);
    }
}
