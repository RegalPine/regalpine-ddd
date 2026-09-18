package io.github.regalpine.ddd.application.result;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationResultTest {

    // -- Test fixtures --

    record PayOrderResult(String orderId, String status) implements ApplicationResult {}

    record EmptyResult() implements ApplicationResult {}

    // -- Tests --

    @Test
    void shouldImplementMarkerInterface() {
        ApplicationResult result = new PayOrderResult("order-1", "PAID");
        assertThat(result).isInstanceOf(ApplicationResult.class);
    }

    @Test
    void shouldSupportRecordEquality() {
        var r1 = new PayOrderResult("order-1", "PAID");
        var r2 = new PayOrderResult("order-1", "PAID");

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    void shouldSupportEmptyResult() {
        ApplicationResult result = new EmptyResult();
        assertThat(result).isInstanceOf(ApplicationResult.class);
    }

    @Test
    void shouldAccessRecordFields() {
        var result = new PayOrderResult("order-1", "PAID");
        assertThat(result.orderId()).isEqualTo("order-1");
        assertThat(result.status()).isEqualTo("PAID");
    }
}
