package io.github.regalpine.ddd.runtime.exception;

/**
 * Thrown when a configuration error is detected during startup.
 *
 * <p>Typical causes: missing required properties, type mismatches,
 * range violations, or conflicting configuration entries.</p>
 *
 * @author RegalPine
 */
public class ConfigurationException extends DddRuntimeException {

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
