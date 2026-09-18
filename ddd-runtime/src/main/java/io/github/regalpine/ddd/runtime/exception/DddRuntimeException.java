package io.github.regalpine.ddd.runtime.exception;

import io.github.regalpine.ddd.core.exception.DddException;

/**
 * Base exception for all runtime-layer errors.
 *
 * <p>The runtime exception hierarchy is:</p>
 * <pre>{@code
 * DddRuntimeException
 *  ├── BootstrapException
 *  ├── ConfigurationException
 *  ├── ComponentException
 *  ├── AdapterException
 *  ├── LifecycleException
 *  └── RuntimeExecutionException
 * }</pre>
 *
 * <p>All runtime exceptions are unchecked and extend {@link DddException}.</p>
 *
 * @author RegalPine
 */
public class DddRuntimeException extends DddException {

    public DddRuntimeException(String message) {
        super(message);
    }

    public DddRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
