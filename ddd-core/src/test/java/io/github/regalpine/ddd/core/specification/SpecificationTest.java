package io.github.regalpine.ddd.core.specification;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpecificationTest {

    @Test
    void shouldEvaluateSimpleSpecification() {
        Specification<Integer> isPositive = n -> n > 0;
        assertThat(isPositive.isSatisfiedBy(5)).isTrue();
        assertThat(isPositive.isSatisfiedBy(-1)).isFalse();
    }

    @Test
    void shouldCombineWithAnd() {
        Specification<Integer> isPositive = n -> n > 0;
        Specification<Integer> isLessThanTen = n -> n < 10;
        Specification<Integer> combined = isPositive.and(isLessThanTen);

        assertThat(combined.isSatisfiedBy(5)).isTrue();
        assertThat(combined.isSatisfiedBy(15)).isFalse();
        assertThat(combined.isSatisfiedBy(-1)).isFalse();
    }

    @Test
    void shouldCombineWithOr() {
        Specification<Integer> isNegative = n -> n < 0;
        Specification<Integer> isGreaterThanTen = n -> n > 10;
        Specification<Integer> combined = isNegative.or(isGreaterThanTen);

        assertThat(combined.isSatisfiedBy(-5)).isTrue();
        assertThat(combined.isSatisfiedBy(15)).isTrue();
        assertThat(combined.isSatisfiedBy(5)).isFalse();
    }

    @Test
    void shouldNegateWithNot() {
        Specification<Integer> isPositive = n -> n > 0;
        Specification<Integer> isNotPositive = isPositive.not();

        assertThat(isNotPositive.isSatisfiedBy(-1)).isTrue();
        assertThat(isNotPositive.isSatisfiedBy(5)).isFalse();
    }

    @Test
    void shouldRejectNullInAnd() {
        Specification<Integer> spec = n -> true;
        assertThatThrownBy(() -> spec.and(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullInOr() {
        Specification<Integer> spec = n -> true;
        assertThatThrownBy(() -> spec.or(null))
                .isInstanceOf(NullPointerException.class);
    }
}
