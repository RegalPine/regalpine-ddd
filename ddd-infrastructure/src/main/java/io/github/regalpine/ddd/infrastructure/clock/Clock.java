package io.github.regalpine.ddd.infrastructure.clock;

import java.time.Instant;

/**
 * Abstraction for obtaining the current time.
 *
 * <p>Domain code should not call {@code Instant.now()} directly.
 * Instead, inject a {@code Clock} port so that tests can use
 * a {@link FixedClock} and production uses a {@link SystemClock}.</p>
 *
 * <p>Per Phase II §53, Clock belongs to {@code ddd-port} (or
 * {@code ddd-infrastructure}), not {@code ddd-core}, because
 * obtaining the current time is an external dependency.</p>
 *
 * @author RegalPine
 */
public interface Clock extends ClockProvider {

    /**
     * Returns the current instant.
     *
     * @return the current instant, never {@code null}
     */
    Instant now();
}
