package io.github.regalpine.ddd.domain.decision;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainDecisionTest {

    // -- Test fixtures --

    record ApprovalDecision(boolean approved, String reason) implements DomainDecision {}

    record PricingDecision(int discountPercent, String tier) implements DomainDecision {}

    // -- Tests --

    @Test
    void shouldCreateApprovalDecision() {
        DomainDecision decision = new ApprovalDecision(true, "Credit score acceptable");

        assertThat(decision).isInstanceOf(DomainDecision.class);
        assertThat(decision).isInstanceOf(ApprovalDecision.class);
    }

    @Test
    void shouldCreateRejectionDecision() {
        ApprovalDecision decision = new ApprovalDecision(false, "Insufficient credit history");

        assertThat(decision.approved()).isFalse();
        assertThat(decision.reason()).isEqualTo("Insufficient credit history");
    }

    @Test
    void shouldCreatePricingDecision() {
        PricingDecision decision = new PricingDecision(15, "GOLD");

        assertThat(decision).isInstanceOf(DomainDecision.class);
        assertThat(decision.discountPercent()).isEqualTo(15);
        assertThat(decision.tier()).isEqualTo("GOLD");
    }

    @Test
    void shouldSupportEqualityForRecordDecisions() {
        var d1 = new ApprovalDecision(true, "OK");
        var d2 = new ApprovalDecision(true, "OK");

        assertThat(d1).isEqualTo(d2);
        assertThat(d1.hashCode()).isEqualTo(d2.hashCode());
    }
}
