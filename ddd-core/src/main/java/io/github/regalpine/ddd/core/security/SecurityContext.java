package io.github.regalpine.ddd.core.security;

import java.util.Optional;

/**
 * Security context providing access to the current principal and permissions.
 *
 * <p>This is a lightweight port — the framework does not implement a full IAM.
 * Concrete identity management can be provided by Spring Security or other
 * identity frameworks.</p>
 *
 * @author RegalPine
 */
public interface SecurityContext {

    /**
     * Returns the identifier of the current principal, if authenticated.
     *
     * @return the principal ID, or empty if unauthenticated
     */
    Optional<String> principalId();

    /**
     * Checks whether the current principal has the given permission.
     *
     * @param permission the permission to check
     * @return {@code true} if the principal has the permission
     */
    boolean hasPermission(String permission);
}
