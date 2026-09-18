package io.github.regalpine.ddd.core.exception;

/**
 * Base exception for all framework-level errors.
 *
 * <p>Phase XI §111: All framework exceptions derive from this base.
 * Subclasses include domain, transaction, messaging, configuration,
 * and concurrency-specific exceptions.</p>
 *
 * <p>Existing {@link DddException} extends this class for backward
 * compatibility.</p>
 *
 * @author RegalPine
 */
public class FrameworkException extends RuntimeException {

    public FrameworkException(String message) {
        super(message);
    }

    public FrameworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
