package io.github.regalpine.ddd.runtime;

import io.github.regalpine.ddd.runtime.configuration.DddConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Default implementation of {@link DddRuntimeBuilder}.
 *
 * <p>Collects components and assembles a {@link DefaultDddRuntime} instance.</p>
 *
 * @author RegalPine
 */
final class DefaultDddRuntimeBuilder implements DddRuntimeBuilder {

    private DddConfiguration configuration;
    private ComponentRegistry componentRegistry;
    private RuntimeConfig config = RuntimeConfig.DEFAULT;
    private final List<Object> components = new ArrayList<>();

    DefaultDddRuntimeBuilder() {
    }

    @Override
    public DddRuntimeBuilder configuration(DddConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
        return this;
    }

    @Override
    public DddRuntimeBuilder componentRegistry(ComponentRegistry registry) {
        this.componentRegistry = Objects.requireNonNull(registry, "componentRegistry must not be null");
        return this;
    }

    @Override
    public DddRuntimeBuilder config(RuntimeConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        return this;
    }

    @Override
    public DddRuntimeBuilder register(Object component) {
        Objects.requireNonNull(component, "component must not be null");
        this.components.add(component);
        return this;
    }

    @Override
    public DddRuntime build() {
        ComponentRegistry registry = this.componentRegistry;
        if (registry == null) {
            registry = new DefaultComponentRegistry();
        }

        // Register individually added components by their type
        // and collect RuntimeComponent instances for lifecycle management
        List<RuntimeComponent> runtimeComponents = new ArrayList<>();
        for (Object component : components) {
            @SuppressWarnings("unchecked")
            Class<Object> type = (Class<Object>) component.getClass();
            registry.register(type, component);
            if (component instanceof RuntimeComponent rc) {
                runtimeComponents.add(rc);
            }
        }

        return new DefaultDddRuntime(registry, config, runtimeComponents);
    }
}
