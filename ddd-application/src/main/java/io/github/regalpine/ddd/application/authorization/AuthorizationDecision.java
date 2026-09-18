package io.github.regalpine.ddd.application.authorization;

/**
 * The outcome of an authorization check.
 *
 * <p>Returned by {@link AuthorizationService} to indicate whether
 * the requested operation is permitted and, if not, why.</p>
 *
 * @param authorized whether the operation is permitted
 * @param reason     a human-readable reason when not authorized, may be {@code null}
 */
public record AuthorizationDecision(
        boolean authorized,
        String reason
) {

    /**
     * Creates an authorized decision.
     *
     * @return a decision indicating the operation is permitted
     */
    public static AuthorizationDecision allow() {
        return new AuthorizationDecision(true, null);
    }

    /**
     * Creates a denied decision with a reason.
     *
     * @param reason the reason for denial
     * @return a decision indicating the operation is not permitted
     */
    public static AuthorizationDecision deny(String reason) {
        return new AuthorizationDecision(false, reason);
    }
}
