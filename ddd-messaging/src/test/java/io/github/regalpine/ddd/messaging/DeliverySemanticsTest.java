package io.github.regalpine.ddd.messaging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DeliverySemanticsTest {

    @Test
    void shouldOnlySupportAtMostOnceAndAtLeastOnce() {
        assertThat(DeliverySemantics.values()).containsExactly(
                DeliverySemantics.AT_MOST_ONCE,
                DeliverySemantics.AT_LEAST_ONCE);
    }

    @Test
    void shouldNotContainExactlyOnce() {
        for (DeliverySemantics ds : DeliverySemantics.values()) {
            assertThat(ds.name()).doesNotContain("EXACTLY");
        }
    }
}
