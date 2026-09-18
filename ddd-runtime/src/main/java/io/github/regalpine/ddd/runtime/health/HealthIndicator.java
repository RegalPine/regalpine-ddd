package io.github.regalpine.ddd.runtime.health;

/**
 * Provides health information for a runtime component or the entire runtime.
 *
 * <p>Implementations report the current health state, which is used
 * by the runtime diagnostics and external health check endpoints.</p>
 *
 * @author RegalPine
 */
public interface HealthIndicator {

    /**
     * Returns the current health state.
     *
     * @return the health state, never {@code null}
     */
    HealthState health();
}
