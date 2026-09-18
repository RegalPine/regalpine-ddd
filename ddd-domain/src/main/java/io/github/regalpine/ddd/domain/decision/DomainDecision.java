package io.github.regalpine.ddd.domain.decision;

/**
 * Marker interface for domain decision results.
 *
 * <p>A Domain Decision represents the outcome of a business policy
 * evaluation. Concrete implementations should be immutable records
 * or value objects that carry the decision details.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * public record ApprovalDecision(
 *         boolean approved,
 *         String reason
 * ) implements DomainDecision {
 * }
 * }</pre>
 *
 * @see io.github.regalpine.ddd.domain.policy.DomainPolicy
 */
public interface DomainDecision {
}
