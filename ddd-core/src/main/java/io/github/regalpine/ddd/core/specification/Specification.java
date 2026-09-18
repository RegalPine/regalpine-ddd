package io.github.regalpine.ddd.core.specification;

import java.util.Objects;

/**
 * A specification pattern for evaluating whether a candidate satisfies
 * a business rule.
 *
 * <p>Specifications are composable using {@link #and(Specification)},
 * {@link #or(Specification)}, and {@link #not()}.</p>
 *
 * <p>Recommended as a {@code @FunctionalInterface} for lambda usage.</p>
 *
 * @param <T> the candidate type to evaluate
 */
@FunctionalInterface
public interface Specification<T> {

    /**
     * Evaluates whether the given candidate satisfies this specification.
     *
     * @param candidate the candidate to evaluate
     * @return {@code true} if the candidate satisfies the specification
     */
    boolean isSatisfiedBy(T candidate);

    /**
     * Returns a new specification that is satisfied when both this
     * specification and the other specification are satisfied.
     *
     * @param other the other specification
     * @return a composed AND specification
     */
    default Specification<T> and(Specification<T> other) {
        Objects.requireNonNull(other, "Other specification must not be null");
        return candidate -> isSatisfiedBy(candidate) && other.isSatisfiedBy(candidate);
    }

    /**
     * Returns a new specification that is satisfied when either this
     * specification or the other specification is satisfied.
     *
     * @param other the other specification
     * @return a composed OR specification
     */
    default Specification<T> or(Specification<T> other) {
        Objects.requireNonNull(other, "Other specification must not be null");
        return candidate -> isSatisfiedBy(candidate) || other.isSatisfiedBy(candidate);
    }

    /**
     * Returns a new specification that is satisfied when this
     * specification is NOT satisfied.
     *
     * @return a negated specification
     */
    default Specification<T> not() {
        return candidate -> !isSatisfiedBy(candidate);
    }
}
