package io.github.regalpine.ddd.runtime;

import io.github.regalpine.ddd.runtime.exception.BootstrapException;
import io.github.regalpine.ddd.runtime.exception.ComponentException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for runtime component lifecycle management.
 */
class RuntimeComponentLifecycleTest {

    @Test
    void shouldStartComponentsInOrder() {
        List<String> startOrder = new ArrayList<>();
        List<String> stopOrder = new ArrayList<>();

        RuntimeComponent a = namedComponent("A", startOrder, stopOrder);
        RuntimeComponent b = namedComponent("B", startOrder, stopOrder);

        DefaultComponentRegistry registry = new DefaultComponentRegistry();
        DefaultDddRuntime runtime = new DefaultDddRuntime(registry, RuntimeConfig.DEFAULT, List.of(a, b));

        runtime.start();
        assertThat(startOrder).containsExactly("A", "B");

        runtime.shutdown();
        assertThat(stopOrder).containsExactly("B", "A");
    }

    @Test
    void shouldDetectDependencyCycle() {
        RuntimeComponentDescriptor descA = new RuntimeComponentDescriptor() {
            @Override public String name() { return "A"; }
            @Override public Set<String> dependencies() { return Set.of("B"); }
        };
        RuntimeComponentDescriptor descB = new RuntimeComponentDescriptor() {
            @Override public String name() { return "B"; }
            @Override public Set<String> dependencies() { return Set.of("A"); }
        };

        // Create components that also implement descriptor
        TestComponent a = new TestComponent("A", descA);
        TestComponent b = new TestComponent("B", descB);

        DefaultComponentRegistry registry = new DefaultComponentRegistry();
        DefaultDddRuntime runtime = new DefaultDddRuntime(registry, RuntimeConfig.DEFAULT, List.of(a, b));

        assertThatThrownBy(runtime::start)
                .isInstanceOf(BootstrapException.class)
                .hasMessageContaining("cycle");
    }

    @Test
    void shouldSetFailedStateOnComponentException() {
        RuntimeComponent failing = new RuntimeComponent() {
            @Override public String name() { return "bad"; }
            @Override public void start(RuntimeContext context) { throw new RuntimeException("fail"); }
            @Override public void stop(RuntimeContext context) {}
        };

        DefaultComponentRegistry registry = new DefaultComponentRegistry();
        DefaultDddRuntime runtime = new DefaultDddRuntime(registry, RuntimeConfig.DEFAULT, List.of(failing));

        assertThatThrownBy(runtime::start).isInstanceOf(ComponentException.class);
        assertThat(runtime.state()).isEqualTo(RuntimeState.FAILED);
    }

    @Test
    void shouldShutdownGracefullyFromFailedState() {
        RuntimeComponent failing = new RuntimeComponent() {
            @Override public String name() { return "bad"; }
            @Override public void start(RuntimeContext context) { throw new RuntimeException("fail"); }
            @Override public void stop(RuntimeContext context) {}
        };

        DefaultComponentRegistry registry = new DefaultComponentRegistry();
        DefaultDddRuntime runtime = new DefaultDddRuntime(registry, RuntimeConfig.DEFAULT, List.of(failing));

        assertThatThrownBy(runtime::start);
        assertThat(runtime.state()).isEqualTo(RuntimeState.FAILED);

        // shutdown from FAILED should be a no-op
        assertThatCode(runtime::shutdown).doesNotThrowAnyException();
        assertThat(runtime.state()).isEqualTo(RuntimeState.STOPPED);
    }

    // --- Helpers ---

    private RuntimeComponent namedComponent(String name, List<String> startOrder, List<String> stopOrder) {
        return new RuntimeComponent() {
            @Override public String name() { return name; }
            @Override public void start(RuntimeContext context) { startOrder.add(name); }
            @Override public void stop(RuntimeContext context) { stopOrder.add(name); }
        };
    }

    private static class TestComponent implements RuntimeComponent, RuntimeComponentDescriptor {
        private final String name;
        private final RuntimeComponentDescriptor descriptor;

        TestComponent(String name, RuntimeComponentDescriptor descriptor) {
            this.name = name;
            this.descriptor = descriptor;
        }

        @Override public String name() { return name; }
        @Override public Set<String> dependencies() { return descriptor.dependencies(); }
        @Override public void start(RuntimeContext context) {}
        @Override public void stop(RuntimeContext context) {}
    }
}
