package io.github.regalpine.ddd.messaging.security;

/**
 * Policy for message security enforcement.
 *
 * <p>Phase IX §50-§55: Message security encompasses authentication, authorization,
 * encryption, integrity, tenant isolation, and audit.</p>
 *
 * <p>Phase IX §52: Signature mechanisms belong to Infrastructure/Security Adapter,
 * not Domain Event.</p>
 *
 * @author RegalPine
 */
public interface MessageSecurityPolicy {

    /**
     * Validates the security of an incoming message.
     *
     * @param messageId the message identifier
     * @param tenantId  the tenant identifier from the message envelope
     * @return true if the message passes security checks
     */
    boolean validate(String messageId, String tenantId);
}
