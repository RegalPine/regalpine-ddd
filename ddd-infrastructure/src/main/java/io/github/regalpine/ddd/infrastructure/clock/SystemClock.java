package io.github.regalpine.ddd.infrastructure.clock;

import java.time.Instant;

/**
 * A {@link Clock} implementation that returns the real system time.
 *
 * @author RegalPine
 */
public final class SystemClock implements Clock, ClockProvider {

    @Override
    public Instant now() {
        return Instant.now();
    }
}
