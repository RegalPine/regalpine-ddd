package io.github.regalpine.ddd.infrastructure.clock;

import java.time.Instant;
import java.util.Objects;

/**
 * A {@link Clock} implementation that always returns a fixed instant.
 *
 * <p>Useful for testing time-dependent domain logic.</p>
 *
 * @author RegalPine
 */
public final class FixedClock implements Clock, ClockProvider {

    private final Instant fixedInstant;

    /**
     * Creates a fixed clock that always returns the given instant.
     *
     * @param fixedInstant the fixed instant, must not be {@code null}
     */
    public FixedClock(Instant fixedInstant) {
        this.fixedInstant = Objects.requireNonNull(fixedInstant, "fixedInstant must not be null");
    }

    @Override
    public Instant now() {
        return fixedInstant;
    }
}
