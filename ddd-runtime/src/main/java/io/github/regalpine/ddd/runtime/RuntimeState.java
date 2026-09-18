package io.github.regalpine.ddd.runtime;

/**
 * Lifecycle states of the DDD runtime.
 *
 * <pre>{@code
 * CREATED → STARTING → RUNNING → STOPPING → STOPPED
 *                  ↘ FAILED
 * }</pre>
 *
 * @author RegalPine
 */
public enum RuntimeState {

    /** The runtime has been created but not yet started. */
    CREATED,

    /** The runtime is in the process of starting. */
    STARTING,

    /** The runtime is fully operational. */
    RUNNING,

    /** The runtime is in the process of shutting down. */
    STOPPING,

    /** The runtime has been shut down. */
    STOPPED,

    /** The runtime failed to start. */
    FAILED
}
