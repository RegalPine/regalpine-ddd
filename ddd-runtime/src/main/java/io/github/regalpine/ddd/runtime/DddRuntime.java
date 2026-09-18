package io.github.regalpine.ddd.runtime;

/**
 * Main entry point interface for the RegalPine DDD runtime.
 *
 * <p>Manages the framework lifecycle:</p>
 * <pre>{@code
 * CREATED → STARTING → RUNNING → STOPPING → STOPPED
 *                  ↘ FAILED
 * }</pre>
 *
 * <p>Supports {@link AutoCloseable} for use in try-with-resources blocks.
 * The {@link #close()} method delegates to {@link #shutdown()}.</p>
 *
 * @author RegalPine
 * @see DddRuntimeBuilder
 * @see RuntimeState
 */
public interface DddRuntime extends AutoCloseable {

    /**
     * Starts the runtime.
     *
     * @throws io.github.regalpine.ddd.runtime.exception.LifecycleException if the runtime cannot start
     */
    void start();

    /**
     * Returns the current lifecycle state.
     *
     * @return the runtime state
     */
    RuntimeState state();

    /**
     * Shuts down the runtime gracefully.
     */
    void shutdown();

    /**
     * Closes the runtime by delegating to {@link #shutdown()}.
     */
    @Override
    default void close() {
        shutdown();
    }

    /**
     * Creates a new runtime builder.
     *
     * @return a new builder instance
     */
    static DddRuntimeBuilder builder() {
        return new DefaultDddRuntimeBuilder();
    }
}
