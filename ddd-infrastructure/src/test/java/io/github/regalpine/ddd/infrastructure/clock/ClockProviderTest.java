package io.github.regalpine.ddd.infrastructure.clock;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClockProviderTest {

    @Test
    void fixedClockProviderShouldReturnFixedInstant() {
        var fixed = Instant.parse("2026-01-01T00:00:00Z");
        var provider = new FixedClockProvider(fixed);
        assertThat(provider.now()).isEqualTo(fixed);
    }

    @Test
    void fixedClockProviderShouldRejectNull() {
        assertThatThrownBy(() -> new FixedClockProvider(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void systemClockProviderShouldReturnNonNullInstant() {
        var provider = new SystemClockProvider();
        assertThat(provider.now()).isNotNull();
    }

    @Test
    void existingClockShouldExtendClockProvider() {
        ClockProvider provider = new FixedClock(Instant.parse("2026-06-01T00:00:00Z"));
        assertThat(provider.now()).isEqualTo(Instant.parse("2026-06-01T00:00:00Z"));
    }

    @Test
    void systemClockShouldImplementClockProvider() {
        ClockProvider provider = new SystemClock();
        assertThat(provider.now()).isNotNull();
    }
}
