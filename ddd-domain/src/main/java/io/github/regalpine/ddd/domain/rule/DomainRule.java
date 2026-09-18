package io.github.regalpine.ddd.domain.rule;

/**
 * Represents a domain validation rule.
 *
 * <p>A Domain Rule validates that a target object satisfies a specific
 * domain constraint. Unlike {@link io.github.regalpine.ddd.core.specification.Specification}
 * which returns a boolean, a Domain Rule communicates failure by throwing
 * a {@link io.github.regalpine.ddd.core.exception.DomainException}.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * public final class OrderTotalMustBePositive implements DomainRule<Order> {
 *     @Override
 *     public void validate(Order order) {
 *         if (order.totalAmount().isNegative()) {
 *             throw new DomainException(OrderError.NEGATIVE_TOTAL);
 *         }
 *     }
 * }
 * }</pre>
 *
 * @param <T> the type of object to validate
 */
public interface DomainRule<T> {

    /**
     * Validates the given target against this rule.
     *
     * <p>If the rule is violated, a {@link io.github.regalpine.ddd.core.exception.DomainException}
     * must be thrown.</p>
     *
     * @param target the object to validate
     * @throws io.github.regalpine.ddd.core.exception.DomainException if validation fails
     */
    void validate(T target);
}
