package io.github.regalpine.ddd.runtime;

/**
 * A manageable component within the DDD runtime.
 *
 * <p>Each runtime component has a stable identity (name) and
 * lifecycle hooks for start and stop.</p>
 *
 * @author RegalPine
 */
public interface RuntimeComponent {

    /**
     * Returns the stable name of this component.
     *
     * @return the component name, never {@code null}
     */
    String name();

    /**
     * Starts this component within the given runtime context.
     *
     * @param context the runtime context
     */
    void start(RuntimeContext context);

    /**
     * Stops this component within the given runtime context.
     *
     * @param context the runtime context
     */
    void stop(RuntimeContext context);
}
