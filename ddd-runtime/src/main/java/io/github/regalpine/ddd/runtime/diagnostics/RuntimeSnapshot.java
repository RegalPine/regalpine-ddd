package io.github.regalpine.ddd.runtime.diagnostics;

import io.github.regalpine.ddd.runtime.RuntimeState;
import io.github.regalpine.ddd.runtime.health.HealthState;

import java.time.Instant;
import java.util.List;

/**
 * A point-in-time snapshot of the runtime state for diagnostics.
 *
 * <p>Contains the runtime state, component states, overall health,
 * and the timestamp when the snapshot was taken.</p>
 *
 * <p>Diagnostics must not leak secrets, credentials, or sensitive payload data.</p>
 *
 * @param state      the runtime lifecycle state
 * @param components the list of component states
 * @param health     the overall health state
 * @param timestamp  the time the snapshot was taken
 * @author RegalPine
 */
public record RuntimeSnapshot(
        RuntimeState state,
        List<ComponentState> components,
        HealthState health,
        Instant timestamp
) {

    public RuntimeSnapshot {
        components = List.copyOf(components);
    }
}
