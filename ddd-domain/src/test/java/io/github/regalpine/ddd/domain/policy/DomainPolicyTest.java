package io.github.regalpine.ddd.domain.policy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainPolicyTest {

    // -- Test fixtures --

    record CreditRequest(int amount) {}

    record CreditDecision(boolean approved, String reason) {}

    static final class CreditLimitPolicy implements DomainPolicy<CreditRequest, CreditDecision> {
        private static final int LIMIT = 10000;

        @Override
        public CreditDecision evaluate(CreditRequest input) {
            if (input.amount() > LIMIT) {
                return new CreditDecision(false, "Amount exceeds credit limit");
            }
            return new CreditDecision(true, "Approved");
        }
    }

    // -- Tests --

    @Test
    void shouldApproveWithinLimit() {
        DomainPolicy<CreditRequest, CreditDecision> policy = new CreditLimitPolicy();
        var result = policy.evaluate(new CreditRequest(5000));

        assertThat(result.approved()).isTrue();
        assertThat(result.reason()).isEqualTo("Approved");
    }

    @Test
    void shouldRejectOverLimit() {
        DomainPolicy<CreditRequest, CreditDecision> policy = new CreditLimitPolicy();
        var result = policy.evaluate(new CreditRequest(15000));

        assertThat(result.approved()).isFalse();
        assertThat(result.reason()).isEqualTo("Amount exceeds credit limit");
    }

    @Test
    void shouldApproveExactLimit() {
        DomainPolicy<CreditRequest, CreditDecision> policy = new CreditLimitPolicy();
        var result = policy.evaluate(new CreditRequest(10000));

        assertThat(result.approved()).isTrue();
    }
}
