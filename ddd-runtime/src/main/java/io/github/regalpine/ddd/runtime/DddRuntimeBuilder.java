package io.github.regalpine.ddd.runtime;

import io.github.regalpine.ddd.runtime.configuration.DddConfiguration;

/**
 * Builder for constructing a {@link DddRuntime} instance.
 *
 * <p>Provides a fluent API for assembling the framework runtime:</p>
 * <pre>{@code
 * DddRuntime runtime = DddRuntimeBuilder.create()
 *     .configuration(configuration)
 *     .componentRegistry(registry)
 *     .build();
 * runtime.start();
 * }</pre>
 *
 * @author RegalPine
 * @see DddRuntime
 */
public interface DddRuntimeBuilder {

    /**
     * Creates a new builder instance.
     *
     * @return a new builder
     */
    static DddRuntimeBuilder create() {
        return new DefaultDddRuntimeBuilder();
    }

    /**
     * Sets the framework configuration.
     *
     * @param configuration the configuration
     * @return this builder
     */
    DddRuntimeBuilder configuration(DddConfiguration configuration);

    /**
     * Sets the component registry containing all runtime components.
     *
     * @param registry the component registry
     * @return this builder
     */
    DddRuntimeBuilder componentRegistry(ComponentRegistry registry);

    /**
     * Sets the runtime configuration.
     *
     * @param config the runtime config
     * @return this builder
     */
    DddRuntimeBuilder config(RuntimeConfig config);

    /**
     * Registers a component with the runtime.
     *
     * @param component the component to register
     * @return this builder
     */
    DddRuntimeBuilder register(Object component);

    /**
     * Builds and returns the {@link DddRuntime} instance.
     *
     * @return the constructed runtime
     */
    DddRuntime build();
}
