package io.github.regalpine.ddd.domain.rule;

import io.github.regalpine.ddd.core.error.DomainError;
import io.github.regalpine.ddd.core.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainRuleTest {

    // -- Test fixtures --

    enum TestError implements DomainError {
        NEGATIVE_AMOUNT("RULE_001", "Amount must be positive"),
        EXCEEDS_LIMIT("RULE_002", "Amount exceeds maximum limit");

        private final String code;
        private final String message;

        TestError(String code, String message) {
            this.code = code;
            this.message = message;
        }

        @Override
        public String code() {
            return code;
        }

        @Override
        public String message() {
            return message;
        }
    }

    record Amount(int value) {}

    static final class PositiveAmountRule implements DomainRule<Amount> {
        @Override
        public void validate(Amount target) {
            if (target.value() <= 0) {
                throw new DomainException(TestError.NEGATIVE_AMOUNT);
            }
        }
    }

    static final class MaxAmountRule implements DomainRule<Amount> {
        private final int max;

        MaxAmountRule(int max) {
            this.max = max;
        }

        @Override
        public void validate(Amount target) {
            if (target.value() > max) {
                throw new DomainException(TestError.EXCEEDS_LIMIT);
            }
        }
    }

    // -- Tests --

    @Test
    void shouldPassWhenRuleSatisfied() {
        DomainRule<Amount> rule = new PositiveAmountRule();
        assertThatCode(() -> rule.validate(new Amount(100)))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowDomainExceptionWhenViolated() {
        DomainRule<Amount> rule = new PositiveAmountRule();

        assertThatThrownBy(() -> rule.validate(new Amount(-1)))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> {
                    DomainException de = (DomainException) ex;
                    org.assertj.core.api.Assertions.assertThat(de.error().code()).isEqualTo("RULE_001");
                });
    }

    @Test
    void shouldThrowOnZero() {
        DomainRule<Amount> rule = new PositiveAmountRule();

        assertThatThrownBy(() -> rule.validate(new Amount(0)))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldSupportParameterizedRule() {
        DomainRule<Amount> rule = new MaxAmountRule(1000);

        assertThatCode(() -> rule.validate(new Amount(500)))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> rule.validate(new Amount(2000)))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> {
                    DomainException de = (DomainException) ex;
                    org.assertj.core.api.Assertions.assertThat(de.error().code()).isEqualTo("RULE_002");
                });
    }
}
