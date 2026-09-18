package io.github.regalpine.ddd.runtime.health;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link HealthState} enum and {@link HealthIndicator} interface.
 */
class HealthIndicatorTest {

    @Test
    void shouldDefineThreeHealthStates() {
        assertThat(HealthState.values()).containsExactly(HealthState.UP, HealthState.DEGRADED, HealthState.DOWN);
    }

    @Test
    void shouldImplementHealthIndicator() {
        HealthIndicator indicator = () -> HealthState.UP;

        assertThat(indicator.health()).isEqualTo(HealthState.UP);
    }
}
