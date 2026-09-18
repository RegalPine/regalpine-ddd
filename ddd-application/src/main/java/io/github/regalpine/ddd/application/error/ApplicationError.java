package io.github.regalpine.ddd.application.error;

/**
 * Marker interface for application-level errors.
 *
 * <p>Phase IV §40 requires three error levels to be distinguished:</p>
 * <ul>
 *   <li>{@code DomainError} — business rule failure (ddd-core)</li>
 *   <li>{@code ApplicationError} — use case execution failure (ddd-application)</li>
 *   <li>Infrastructure error — technology failure (ddd-infrastructure)</li>
 * </ul>
 *
 * <p>Concrete application errors should implement this interface
 * to provide structured error information. Example:</p>
 * <pre>{@code
 * public enum OrderAppError implements ApplicationError {
 *     ORDER_NOT_FOUND("APP_ORDER_001", "Order not found"),
 *     DUPLICATE_PAYMENT("APP_ORDER_002", "Payment already processed");
 *
 *     private final String code;
 *     private final String message;
 *     // constructor + accessors
 * }
 * }</pre>
 *
 * @see io.github.regalpine.ddd.core.error.DomainError
 */
public interface ApplicationError {

    /**
     * Returns the stable error code.
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
