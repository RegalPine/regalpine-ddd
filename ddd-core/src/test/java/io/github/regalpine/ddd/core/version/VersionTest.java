package io.github.regalpine.ddd.core.version;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VersionTest {

    @Test
    void shouldCreateVersionWithNonNegativeValue() {
        var version = new Version(0);
        assertThat(version.value()).isZero();
    }

    @Test
    void shouldCreateInitialVersionAsZero() {
        var version = Version.initial();
        assertThat(version.value()).isZero();
    }

    @Test
    void shouldReturnNextVersionByIncrementing() {
        var version = new Version(0);
        var next = version.next();
        assertThat(next.value()).isEqualTo(1);
    }

    @Test
    void shouldChainNextCalls() {
        var version = Version.initial();
        assertThat(version.next().next().next().value()).isEqualTo(3);
    }

    @Test
    void shouldRejectNegativeVersion() {
        assertThatThrownBy(() -> new Version(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be negative");
    }

    @Test
    void shouldSupportValueEquality() {
        assertThat(new Version(5)).isEqualTo(new Version(5));
    }

    @Test
    void shouldSupportValueInequality() {
        assertThat(new Version(5)).isNotEqualTo(new Version(6));
    }
}
