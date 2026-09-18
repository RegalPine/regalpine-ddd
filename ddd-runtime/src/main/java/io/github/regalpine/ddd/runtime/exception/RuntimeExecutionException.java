package io.github.regalpine.ddd.runtime.exception;

/**
 * Thrown when a runtime execution error occurs during command or query processing.
 *
 * <p>Typical causes: command dispatch failure, query execution error,
 * or transaction execution failure.</p>
 *
 * @author RegalPine
 */
public class RuntimeExecutionException extends DddRuntimeException {

    public RuntimeExecutionException(String message) {
        super(message);
    }

    public RuntimeExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
