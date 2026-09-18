package io.github.regalpine.ddd.runtime;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link ComponentRegistry} interface contract.
 */
class ComponentRegistryTest {

    @Test
    void shouldRegisterAndFindComponent() {
        DefaultComponentRegistry registry = new DefaultComponentRegistry();
        registry.register(String.class, "hello");

        assertThat(registry.find(String.class)).contains("hello");
    }

    @Test
    void shouldRequireRegisteredComponent() {
        DefaultComponentRegistry registry = new DefaultComponentRegistry();
        registry.register(Integer.class, 42);

        assertThat(registry.require(Integer.class)).isEqualTo(42);
    }

    @Test
    void shouldReturnEmptyForUnregisteredType() {
        DefaultComponentRegistry registry = new DefaultComponentRegistry();

        assertThat(registry.find(String.class)).isEmpty();
    }

    @Test
    void shouldThrowOnRequireUnregistered() {
        DefaultComponentRegistry registry = new DefaultComponentRegistry();

        assertThatThrownBy(() -> registry.require(String.class))
                .isInstanceOf(java.util.NoSuchElementException.class);
    }
}
