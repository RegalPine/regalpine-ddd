package io.github.regalpine.ddd.domain.policy;

/**
 * Represents a stable business policy or decision rule.
 *
 * <p>A Domain Policy encapsulates a business rule that takes input
 * and produces a decision or result. Policies should be pure domain
 * logic with no infrastructure dependencies.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * public final class CreditLimitPolicy implements DomainPolicy<CreditRequest, CreditDecision> {
 *     @Override
 *     public CreditDecision evaluate(CreditRequest request) {
 *         // pure business rule evaluation
 *     }
 * }
 * }</pre>
 *
 * @param <T> the input type
 * @param <R> the result type
 */
public interface DomainPolicy<T, R> {

    /**
     * Evaluates the policy against the given input.
     *
     * @param input the input to evaluate
     * @return the policy result
     */
    R evaluate(T input);
}
