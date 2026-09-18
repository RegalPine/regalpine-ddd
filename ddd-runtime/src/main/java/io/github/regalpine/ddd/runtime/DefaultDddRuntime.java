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

/**
 * Default implementation of the {@link DddRuntime} interface.
 *
 * <p>Provides full component lifecycle management with dependency-ordered
 * startup and reverse-order shutdown. Detects dependency cycles during
 * bootstrap and fails fast.</p>
 *
 * <pre>{@code
 * DddRuntime runtime = DddRuntime.builder()
 *     .componentRegistry(registry)
 *     .config(config)
 *     .build();
 * runtime.start();
 * }</pre>
 *
 * @author RegalPine
 */
public final class DefaultDddRuntime implements DddRuntime, RuntimeDiagnostics {

    private final ComponentRegistry componentRegistry;
    private final RuntimeConfig config;
    private final List<RuntimeComponent> components;
    private volatile RuntimeState state;

    DefaultDddRuntime(ComponentRegistry componentRegistry, RuntimeConfig config) {
        this(componentRegistry, config, List.of());
    }

    DefaultDddRuntime(ComponentRegistry componentRegistry, RuntimeConfig config,
                      List<RuntimeComponent> components) {
        this.componentRegistry = componentRegistry;
        this.config = config;
        this.components = new ArrayList<>(components);
        this.state = RuntimeState.CREATED;
    }

    @Override
    public void start() {
        if (state != RuntimeState.CREATED) {
            throw new LifecycleException("Cannot start runtime in state: " + state);
        }
        state = RuntimeState.STARTING;
        try {
            // Build dependency descriptors and check for cycles
            List<RuntimeComponentDescriptor> descriptors = collectDescriptors();
            detectCycles(descriptors);

            // Topological sort and start components in order
            List<RuntimeComponent> sorted = topologicalSort(descriptors);
            RuntimeContext context = buildContext();
            for (RuntimeComponent component : sorted) {
                try {
                    component.start(context);
                } catch (Exception e) {
                    state = RuntimeState.FAILED;
                    throw new ComponentException(
                            "Failed to start component: " + component.name(), e);
                }
            }
            state = RuntimeState.RUNNING;
        } catch (ComponentException e) {
            throw e;
        } catch (BootstrapException e) {
            state = RuntimeState.FAILED;
            throw e;
        } catch (Exception e) {
            state = RuntimeState.FAILED;
            throw new BootstrapException("Runtime bootstrap failed", e);
        }
    }

    @Override
    public RuntimeState state() {
        return state;
    }

    @Override
    public void shutdown() {
        // Gracefully handle non-running states
        if (state == RuntimeState.STOPPED || state == RuntimeState.CREATED) {
            return;
        }
        if (state == RuntimeState.FAILED) {
            state = RuntimeState.STOPPED;
            return;
        }
        if (state != RuntimeState.RUNNING) {
            throw new LifecycleException("Cannot shutdown runtime in state: " + state);
        }
        state = RuntimeState.STOPPING;
        try {
            // Stop components in reverse order
            RuntimeContext context = buildContext();
            List<RuntimeComponent> reversed = new ArrayList<>(components);
            Collections.reverse(reversed);
            for (RuntimeComponent component : reversed) {
                try {
                    component.stop(context);
                } catch (Exception e) {
                    // Log but continue stopping other components
                }
            }
        } finally {
            state = RuntimeState.STOPPED;
        }
    }

    @Override
    public RuntimeSnapshot snapshot() {
        List<ComponentState> componentStates = components.stream()
                .map(c -> new ComponentState(c.name(), state))
                .toList();
        HealthState health = resolveHealth();
        return new RuntimeSnapshot(state, componentStates, health, Instant.now());
    }

    /**
     * Returns the component registry.
     */
    public ComponentRegistry componentRegistry() {
        return componentRegistry;
    }

    /**
     * Returns the runtime configuration.
     */
    public RuntimeConfig config() {
        return config;
    }

    /**
     * Returns the command bus from the component registry.
     */
    public CommandBus commandBus() {
        return componentRegistry.commandBus();
    }

    /**
     * Returns the query bus from the component registry.
     */
    public QueryBus queryBus() {
        return componentRegistry.queryBus();
    }

    // --- Internal lifecycle helpers ---

    private RuntimeContext buildContext() {
        if (componentRegistry instanceof RuntimeContext rc) {
            return rc;
        }
        // Fallback: create a simple context backed by the registry
        return new RuntimeContext() {
            @Override
            public <T> Optional<T> get(Class<T> type) {
                return componentRegistry.find(type);
            }

            @Override
            public <T> T require(Class<T> type) {
                return componentRegistry.require(type);
            }
        };
    }

    private List<RuntimeComponentDescriptor> collectDescriptors() {
        List<RuntimeComponentDescriptor> descriptors = new ArrayList<>();
        for (RuntimeComponent component : components) {
            if (component instanceof RuntimeComponentDescriptor descriptor) {
                descriptors.add(descriptor);
            }
        }
        return descriptors;
    }

    /**
     * Detects dependency cycles using DFS with coloring.
     * WHITE=unvisited, GRAY=in-progress, BLACK=done.
     */
    private void detectCycles(List<RuntimeComponentDescriptor> descriptors) {
        Map<String, List<String>> graph = new HashMap<>();
        Set<String> allNames = new HashSet<>();
        for (RuntimeComponentDescriptor d : descriptors) {
            graph.put(d.name(), new ArrayList<>(d.dependencies()));
            allNames.add(d.name());
        }

        Map<String, Integer> color = new HashMap<>(); // 0=WHITE, 1=GRAY, 2=BLACK
        for (String name : allNames) {
            color.put(name, 0);
        }

        for (String name : allNames) {
            if (color.get(name) == 0) {
                if (hasCycleDfs(name, graph, color, new ArrayDeque<>())) {
                    throw new BootstrapException("Dependency cycle detected in runtime components");
                }
            }
        }
    }

    private boolean hasCycleDfs(String node, Map<String, List<String>> graph,
                                Map<String, Integer> color, Deque<String> path) {
        color.put(node, 1); // GRAY
        path.push(node);

        List<String> deps = graph.getOrDefault(node, List.of());
        for (String dep : deps) {
            if (!color.containsKey(dep)) {
                continue; // dependency not in graph, skip
            }
            if (color.get(dep) == 1) {
                return true; // back edge = cycle
            }
            if (color.get(dep) == 0 && hasCycleDfs(dep, graph, color, path)) {
                return true;
            }
        }

        path.pop();
        color.put(node, 2); // BLACK
        return false;
    }

    /**
     * Topological sort using Kahn's algorithm.
     * Returns components in dependency order (dependencies first).
     */
    private List<RuntimeComponent> topologicalSort(List<RuntimeComponentDescriptor> descriptors) {
        if (descriptors.isEmpty()) {
            return new ArrayList<>(components);
        }

        // Build name -> component mapping
        Map<String, RuntimeComponent> nameToComponent = new HashMap<>();
        for (RuntimeComponent c : components) {
            nameToComponent.put(c.name(), c);
        }

        // Build adjacency and in-degree
        Map<String, Set<String>> adjacency = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();
        Set<String> allNames = new HashSet<>();

        for (RuntimeComponentDescriptor d : descriptors) {
            allNames.add(d.name());
            adjacency.putIfAbsent(d.name(), new HashSet<>());
            inDegree.putIfAbsent(d.name(), 0);
            for (String dep : d.dependencies()) {
                if (allNames.contains(dep) || descriptors.stream().anyMatch(x -> x.name().equals(dep))) {
                    adjacency.computeIfAbsent(dep, k -> new HashSet<>()).add(d.name());
                    inDegree.merge(d.name(), 1, Integer::sum);
                    allNames.add(dep);
                }
            }
        }

        // BFS
        Queue<String> queue = new ArrayDeque<>();
        for (String name : allNames) {
            if (inDegree.getOrDefault(name, 0) == 0) {
                queue.add(name);
            }
        }

        List<RuntimeComponent> sorted = new ArrayList<>();
        while (!queue.isEmpty()) {
            String name = queue.poll();
            RuntimeComponent component = nameToComponent.get(name);
            if (component != null) {
                sorted.add(component);
            }
            for (String neighbor : adjacency.getOrDefault(name, Set.of())) {
                int newDegree = inDegree.merge(neighbor, -1, Integer::sum);
                if (newDegree == 0) {
                    queue.add(neighbor);
                }
            }
        }

        // Add any components not in the descriptor list (no dependencies)
        Set<String> sortedNames = new HashSet<>();
        for (RuntimeComponent c : sorted) {
            sortedNames.add(c.name());
        }
        for (RuntimeComponent c : components) {
            if (!sortedNames.contains(c.name())) {
                sorted.add(c);
            }
        }

        return sorted;
    }

    private HealthState resolveHealth() {
        if (state != RuntimeState.RUNNING) {
            return HealthState.DOWN;
        }
        // Check all registered HealthIndicators
        HealthIndicator indicator = componentRegistry.find(HealthIndicator.class).orElse(null);
        if (indicator != null) {
            return indicator.health();
        }
        return HealthState.UP;
    }
}
