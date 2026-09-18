package io.github.regalpine.ddd.core.error;

/**
 * Represents a unified framework-level error with a stable code and message.
 *
 * <p>Phase X §58: All framework errors must implement this interface to provide
 * a consistent error model across all layers (Domain, Application, Infrastructure,
 * Messaging, Runtime).</p>
 *
 * <p>Error codes must be stable and suitable for programmatic handling.
 * Infrastructure exceptions (SQLException, KafkaException, etc.) must never
 * be exposed directly to the Domain layer.</p>
 *
 * @author RegalPine
 */
public interface FrameworkError {

    /**
     * Returns the stable error code for programmatic handling.
     *
     * @return the error code, never {@code null}
     */
    String code();

    /**
     * Returns a human-readable error message.
     *
     * @return the error message, never {@code null}
     */
    String message();
}
