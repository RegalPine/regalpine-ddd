package io.github.regalpine.ddd.runtime.exception;

/**
 * Thrown when an illegal lifecycle state transition is attempted.
 *
 * <p>For example: starting a runtime that is already running,
 * or shutting down a runtime that has not been started.</p>
 *
 * @author RegalPine
 */
public class LifecycleException extends DddRuntimeException {

    public LifecycleException(String message) {
        super(message);
    }

    public LifecycleException(String message, Throwable cause) {
        super(message, cause);
    }
}
