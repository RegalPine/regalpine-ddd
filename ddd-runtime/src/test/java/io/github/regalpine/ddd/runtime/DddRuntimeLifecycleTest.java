package io.github.regalpine.ddd.runtime;

import io.github.regalpine.ddd.runtime.exception.LifecycleException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link DddRuntime} lifecycle state transitions.
 */
class DddRuntimeLifecycleTest {

    @Test
    void shouldStopInReverseActualStartupOrder() {
        var calls = new java.util.ArrayList<String>();
        var resource = new TestComponent("resource", java.util.Set.of(), calls, false, false);
        var user = new TestComponent("user", java.util.Set.of("resource"), calls, false, false);
        var runtime = new DefaultDddRuntime(registry(), RuntimeConfig.DEFAULT, java.util.List.of(user, resource));
        runtime.start();
        runtime.shutdown();
        runtime.shutdown();
        assertThat(calls).containsExactly("start:resource", "start:user", "stop:user", "stop:resource");
    }

    @Test
    void shouldCleanPartialStartupAndPreserveCleanupFailure() {
        var calls = new java.util.ArrayList<String>();
        var resource = new TestComponent("resource", java.util.Set.of(), calls, false, true);
        var user = new TestComponent("user", java.util.Set.of("resource"), calls, true, false);
        var runtime = new DefaultDddRuntime(registry(), RuntimeConfig.DEFAULT, java.util.List.of(user, resource));
        assertThatThrownBy(runtime::start).satisfies(e -> assertThat(e.getSuppressed()).hasSize(1));
        assertThat(calls).containsExactly("start:resource", "start:user", "stop:resource");
        runtime.shutdown();
        assertThat(runtime.state()).isEqualTo(RuntimeState.STOPPED);
        assertThat(calls).hasSize(3);
    }

    @Test
    void shouldContinueShutdownAfterFailure() {
        var calls = new java.util.ArrayList<String>();
        var resource = new TestComponent("resource", java.util.Set.of(), calls, false, false);
        var user = new TestComponent("user", java.util.Set.of("resource"), calls, false, true);
        var runtime = new DefaultDddRuntime(registry(), RuntimeConfig.DEFAULT, java.util.List.of(user, resource));
        runtime.start();
        assertThatThrownBy(runtime::shutdown).isInstanceOf(LifecycleException.class);
        assertThat(calls).endsWith("stop:user", "stop:resource");
        assertThat(runtime.state()).isEqualTo(RuntimeState.STOPPED);
    }

    private record TestComponent(String name, java.util.Set<String> dependencies,
                                 java.util.List<String> calls, boolean failStart, boolean failStop)
            implements RuntimeComponent, RuntimeComponentDescriptor {
        public void start(RuntimeContext context) {
            calls.add("start:" + name);
            if (failStart) throw new IllegalStateException("start");
        }
        public void stop(RuntimeContext context) {
            calls.add("stop:" + name);
            if (failStop) throw new IllegalStateException("stop");
        }
    }

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
