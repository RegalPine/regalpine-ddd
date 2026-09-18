package io.github.regalpine.ddd.runtime;

import io.github.regalpine.ddd.cqrs.bus.CommandBus;
import io.github.regalpine.ddd.cqrs.bus.QueryBus;
import io.github.regalpine.ddd.runtime.diagnostics.ComponentState;
import io.github.regalpine.ddd.runtime.diagnostics.RuntimeDiagnostics;
import io.github.regalpine.ddd.runtime.diagnostics.RuntimeSnapshot;
import io.github.regalpine.ddd.runtime.exception.BootstrapException;
import io.github.regalpine.ddd.runtime.exception.ComponentException;
import io.github.regalpine.ddd.runtime.exception.LifecycleException;
import io.github.regalpine.ddd.runtime.health.HealthIndicator;
import io.github.regalpine.ddd.runtime.health.HealthState;

import java.time.Instant;
import java.util.*;

/** 按依赖启动，并严格按实际启动顺序逆序清理组件。 */
public final class DefaultDddRuntime implements DddRuntime, RuntimeDiagnostics {
    private final ComponentRegistry componentRegistry;
    private final RuntimeConfig config;
    private final List<RuntimeComponent> components;
    private final List<RuntimeComponent> started = new ArrayList<>();
    private volatile RuntimeState state = RuntimeState.CREATED;

    DefaultDddRuntime(ComponentRegistry registry, RuntimeConfig config) {
        this(registry, config, List.of());
    }

    DefaultDddRuntime(ComponentRegistry registry, RuntimeConfig config, List<RuntimeComponent> components) {
        this.componentRegistry = Objects.requireNonNull(registry, "registry");
        this.config = Objects.requireNonNull(config, "config");
        this.components = List.copyOf(components);
    }

    @Override
    public synchronized void start() {
        if (state != RuntimeState.CREATED) {
            throw new LifecycleException("Cannot start runtime in state: " + state);
        }
        state = RuntimeState.STARTING;
        try {
            RuntimeContext context = buildContext();
            for (RuntimeComponent component : orderedComponents()) {
                try {
                    component.start(context);
                    started.add(component);
                } catch (Exception failure) {
                    throw new ComponentException("Failed to start component: " + component.name(), failure);
                }
            }
            state = RuntimeState.RUNNING;
        } catch (RuntimeException | Error failure) {
            state = RuntimeState.FAILED;
            stopStarted(failure);
            throw failure;
        }
    }

    @Override
    public RuntimeState state() {
        return state;
    }

    @Override
    public synchronized void shutdown() {
        if (state == RuntimeState.STOPPED || state == RuntimeState.CREATED) {
            return;
        }
        if (state != RuntimeState.RUNNING && state != RuntimeState.FAILED) {
            throw new LifecycleException("Cannot shutdown runtime in state: " + state);
        }
        state = RuntimeState.STOPPING;
        LifecycleException failure = new LifecycleException("组件关闭失败");
        try {
            stopStarted(failure);
        } finally {
            state = RuntimeState.STOPPED;
        }
        if (failure.getSuppressed().length > 0) {
            throw failure;
        }
    }

    private void stopStarted(Throwable failure) {
        RuntimeContext context = buildContext();
        for (int i = started.size() - 1; i >= 0; i--) {
            RuntimeComponent component = started.remove(i);
            try {
                component.stop(context);
            } catch (Throwable cleanup) {
                failure.addSuppressed(cleanup);
            }
        }
    }

    private List<RuntimeComponent> orderedComponents() {
        Map<String, RuntimeComponent> byName = new LinkedHashMap<>();
        for (RuntimeComponent component : components) {
            if (byName.putIfAbsent(component.name(), component) != null) {
                throw new BootstrapException("组件名称重复: " + component.name());
            }
        }
        List<RuntimeComponent> ordered = new ArrayList<>();
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();
        for (String name : byName.keySet()) {
            visit(name, byName, visiting, visited, ordered);
        }
        return ordered;
    }

    private void visit(String name, Map<String, RuntimeComponent> byName, Set<String> visiting,
                       Set<String> visited, List<RuntimeComponent> ordered) {
        if (visited.contains(name)) {
            return;
        }
        RuntimeComponent component = byName.get(name);
        if (component == null) {
            throw new BootstrapException("缺少组件依赖: " + name);
        }
        if (!visiting.add(name)) {
            throw new BootstrapException("Dependency cycle detected: " + name);
        }
        if (component instanceof RuntimeComponentDescriptor descriptor) {
            for (String dependency : descriptor.dependencies()) {
                visit(dependency, byName, visiting, visited, ordered);
            }
        }
        visiting.remove(name);
        visited.add(name);
        ordered.add(component);
    }

    @Override
    public RuntimeSnapshot snapshot() {
        List<ComponentState> states = components.stream()
                .map(c -> new ComponentState(c.name(), state)).toList();
        HealthState health = state == RuntimeState.RUNNING
                ? componentRegistry.find(HealthIndicator.class).map(HealthIndicator::health).orElse(HealthState.UP)
                : HealthState.DOWN;
        return new RuntimeSnapshot(state, states, health, Instant.now());
    }

    public ComponentRegistry componentRegistry() { return componentRegistry; }
    public RuntimeConfig config() { return config; }
    public CommandBus commandBus() { return componentRegistry.commandBus(); }
    public QueryBus queryBus() { return componentRegistry.queryBus(); }

    private RuntimeContext buildContext() {
        if (componentRegistry instanceof RuntimeContext context) {
            return context;
        }
        return new RuntimeContext() {
            @Override
            public <T> Optional<T> get(Class<T> type) { return componentRegistry.find(type); }
            @Override
            public <T> T require(Class<T> type) { return componentRegistry.require(type); }
        };
    }
}
