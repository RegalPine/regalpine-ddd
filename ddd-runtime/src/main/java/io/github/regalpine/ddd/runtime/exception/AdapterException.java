package io.github.regalpine.ddd.runtime.exception;

/**
 * Thrown when an adapter encounters an error during initialization or operation.
 *
 * <p>Typical causes: adapter initialization failure, required adapter unavailable,
 * or adapter communication error.</p>
 *
 * @author RegalPine
 */
public class AdapterException extends DddRuntimeException {

    public AdapterException(String message) {
        super(message);
    }

    public AdapterException(String message, Throwable cause) {
        super(message, cause);
    }
}
