package io.github.regalpine.ddd.application.authorization;

/**
 * Port for authorization checks in the application layer.
 *
 * <p>Used by authorization middleware to verify that the current
 * user has permission to execute a given command or query.
 * Infrastructure adapters provide the concrete implementation
 * (e.g. RBAC, ABAC, external IAM service).</p>
 *
 * @see AuthorizationRequest
 * @see AuthorizationDecision
 */
public interface AuthorizationService {

    /**
     * Evaluates whether the requested operation is authorized.
     *
     * @param request the authorization request containing operation,
     *                principal, and tenant context
     * @return the authorization decision, never {@code null}
     */
    AuthorizationDecision authorize(AuthorizationRequest request);
}
