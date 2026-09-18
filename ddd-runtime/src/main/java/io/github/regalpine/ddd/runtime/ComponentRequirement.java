package io.github.regalpine.ddd.runtime;

/**
 * Declares whether a component is required or optional for runtime operation.
 *
 * <p>Required components must be available for the runtime to start.
 * Optional component failure results in DEGRADED state rather than startup failure.</p>
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>Database — Required</li>
 *   <li>Transaction — Required</li>
 *   <li>Cache — Optional</li>
 *   <li>Metrics — Optional</li>
 *   <li>Tracing — Optional</li>
 * </ul>
 *
 * @author RegalPine
 */
public interface ComponentRequirement {

    /**
     * Returns whether this component is required for runtime operation.
     *
     * @return {@code true} if required, {@code false} if optional
     */
    boolean required();
}
