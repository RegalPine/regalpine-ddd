package io.github.regalpine.ddd.runtime.exception;

import io.github.regalpine.ddd.core.exception.DddException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the {@link DddRuntimeException} hierarchy.
 */
class DddRuntimeExceptionTest {

    @Test
    void shouldExtendDddException() {
        assertThat(new BootstrapException("test")).isInstanceOf(DddException.class);
        assertThat(new ConfigurationException("test")).isInstanceOf(DddException.class);
        assertThat(new ComponentException("test")).isInstanceOf(DddException.class);
        assertThat(new AdapterException("test")).isInstanceOf(DddException.class);
        assertThat(new LifecycleException("test")).isInstanceOf(DddException.class);
        assertThat(new RuntimeExecutionException("test")).isInstanceOf(DddException.class);
    }

    @Test
    void shouldCarryMessageAndCause() {
        Throwable cause = new RuntimeException("root cause");
        BootstrapException ex = new BootstrapException("bootstrap failed", cause);

        assertThat(ex.getMessage()).isEqualTo("bootstrap failed");
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void shouldAllBeUnchecked() {
        // All runtime exceptions should be RuntimeException (unchecked)
        assertThat(new DddRuntimeException("test")).isInstanceOf(RuntimeException.class);
        assertThat(new BootstrapException("test")).isInstanceOf(RuntimeException.class);
        assertThat(new LifecycleException("test")).isInstanceOf(RuntimeException.class);
    }
}
