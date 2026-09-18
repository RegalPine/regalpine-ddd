package io.github.regalpine.ddd.runtime.exception;

/**
 * Thrown when the runtime fails to bootstrap.
 *
 * <p>Typical causes: missing required components, unsatisfied dependencies,
 * or dependency cycles detected during startup.</p>
 *
 * @author RegalPine
 */
public class BootstrapException extends DddRuntimeException {

    public BootstrapException(String message) {
        super(message);
    }

    public BootstrapException(String message, Throwable cause) {
        super(message, cause);
    }
}
