package io.github.regalpine.ddd.application.authorization;

import java.util.Objects;

/**
 * Request for an authorization check.
 *
 * <p>Carries the operation identifier along with the principal
 * and tenant context needed to make an authorization decision.</p>
 *
 * @param operation   the operation identifier (e.g. "PayOrder", "CancelOrder")
 * @param principalId the authenticated user identifier, may be {@code null}
 * @param tenantId    the tenant identifier, may be {@code null}
 */
public record AuthorizationRequest(
        String operation,
        String principalId,
        String tenantId
) {
    public AuthorizationRequest {
        Objects.requireNonNull(operation, "Operation must not be null");
    }

    /**
     * Creates a request with only an operation identifier.
     *
     * @param operation the operation identifier
     */
    public AuthorizationRequest(String operation) {
        this(operation, null, null);
    }
}
