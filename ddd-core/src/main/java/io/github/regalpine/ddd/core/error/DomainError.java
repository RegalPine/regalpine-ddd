package io.github.regalpine.ddd.core.error;

/**
 * Represents a domain-level error with a stable error code and message.
 *
 * <p>Domain errors are used by {@link io.github.regalpine.ddd.core.exception.DomainException}
 * to carry structured error information. Error codes should be stable
 * strings suitable for programmatic handling.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * public enum OrderError implements DomainError {
 *     ORDER_NOT_FOUND("ORDER_001", "Order not found"),
 *     ORDER_ALREADY_PAID("ORDER_002", "Order has already been paid"),
 *     INVALID_ORDER_STATUS("ORDER_003", "Invalid order status transition");
 *
 *     private final String code;
 *     private final String message;
 *     // constructor + accessors
 * }
 * }</pre>
 */
public interface DomainError {

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
