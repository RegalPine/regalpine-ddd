package io.github.regalpine.ddd.infrastructure.clock;

import java.time.Instant;

/**
 * Abstraction for obtaining the current time.
 *
 * <p>Phase XI §114: ClockProvider is the framework-standard name for
 * time abstraction. The existing {@link Clock} interface extends this
 * for backward compatibility.</p>
 *
 * @author RegalPine
 */
public interface ClockProvider {

    /**
     * Returns the current instant.
     *
     * @return the current instant, never {@code null}
     */
    Instant now();
}
