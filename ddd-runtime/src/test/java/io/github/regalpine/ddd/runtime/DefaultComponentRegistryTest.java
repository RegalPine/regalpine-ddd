package io.github.regalpine.ddd.runtime;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link DefaultComponentRegistry} implementation.
 */
class DefaultComponentRegistryTest {

    @Test
    void shouldRejectNullType() {
        DefaultComponentRegistry registry = new DefaultComponentRegistry();

        assertThatThrownBy(() -> registry.register(null, "value"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("type");
    }

    @Test
    void shouldRejectNullComponent() {
        DefaultComponentRegistry registry = new DefaultComponentRegistry();

        assertThatThrownBy(() -> registry.register(String.class, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("component");
    }

    @Test
    void shouldOverwritePreviousRegistration() {
        DefaultComponentRegistry registry = new DefaultComponentRegistry();
        registry.register(String.class, "first");
        registry.register(String.class, "second");

        assertThat(registry.require(String.class)).isEqualTo("second");
    }
}
