package io.github.regalpine.ddd.runtime.diagnostics;

import io.github.regalpine.ddd.runtime.RuntimeState;

/**
 * Represents the state of a single runtime component at a point in time.
 *
 * @param name  the component name
 * @param state the component lifecycle state
 * @author RegalPine
 */
public record ComponentState(String name, RuntimeState state) {
}
