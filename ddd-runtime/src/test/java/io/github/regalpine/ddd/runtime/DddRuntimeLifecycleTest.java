package io.github.regalpine.ddd.runtime;

import io.github.regalpine.ddd.runtime.exception.LifecycleException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link DddRuntime} lifecycle state transitions.
 */
class DddRuntimeLifecycleTest {

    private DefaultComponentRegistry registry() {
        return new DefaultComponentRegistry();
    }

    @Test
    void shouldTransitionThroughNormalLifecycle() {
        DddRuntime runtime = new DefaultDddRuntime(registry(), RuntimeConfig.DEFAULT);

        assertThat(runtime.state()).isEqualTo(RuntimeState.CREATED);

        runtime.start();
        assertThat(runtime.state()).isEqualTo(RuntimeState.RUNNING);

        runtime.shutdown();
        assertThat(runtime.state()).isEqualTo(RuntimeState.STOPPED);
    }

    @Test
    void shouldFailWhenComponentStartFails() {
        RuntimeComponent failing = new RuntimeComponent() {
            @Override public String name() { return "failing"; }
            @Override public void start(RuntimeContext context) {
                throw new RuntimeException("boom");
            }
            @Override public void stop(RuntimeContext context) {}
        };

        DddRuntime runtime = new DefaultDddRuntime(registry(), RuntimeConfig.DEFAULT, java.util.List.of(failing));

        assertThatThrownBy(runtime::start)
                .isInstanceOf(io.github.regalpine.ddd.runtime.exception.ComponentException.class)
                .hasMessageContaining("failing");

        assertThat(runtime.state()).isEqualTo(RuntimeState.FAILED);
    }

    @Test
    void shouldSupportAutoCloseable() {
        DddRuntime runtime = new DefaultDddRuntime(registry(), RuntimeConfig.DEFAULT);
        runtime.start();
        assertThat(runtime.state()).isEqualTo(RuntimeState.RUNNING);

        // close() should delegate to shutdown()
        runtime.close();
        assertThat(runtime.state()).isEqualTo(RuntimeState.STOPPED);
    }

    @Test
    void shouldThrowLifecycleExceptionOnInvalidStart() {
        DddRuntime runtime = new DefaultDddRuntime(registry(), RuntimeConfig.DEFAULT);
        runtime.start();

        assertThatThrownBy(runtime::start)
                .isInstanceOf(LifecycleException.class)
                .hasMessageContaining("RUNNING");
    }

    @Test
    void shouldHandleShutdownFromCreatedState() {
        DddRuntime runtime = new DefaultDddRuntime(registry(), RuntimeConfig.DEFAULT);

        // shutdown from CREATED should be a no-op
        assertThatCode(runtime::shutdown).doesNotThrowAnyException();
        assertThat(runtime.state()).isEqualTo(RuntimeState.CREATED);
    }
}
