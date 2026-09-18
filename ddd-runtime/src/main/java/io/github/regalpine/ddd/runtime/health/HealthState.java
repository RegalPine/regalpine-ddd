package io.github.regalpine.ddd.runtime.health;

/**
 * Represents the health state of a runtime component or the entire runtime.
 *
 * @author RegalPine
 */
public enum HealthState {

    /** The component or runtime is fully operational. */
    UP,

    /** The component or runtime is operational but with degraded performance or functionality. */
    DEGRADED,

    /** The component or runtime is not operational. */
    DOWN
}
