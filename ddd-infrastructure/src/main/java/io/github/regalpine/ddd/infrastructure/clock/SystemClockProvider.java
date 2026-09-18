package io.github.regalpine.ddd.infrastructure.clock;

import java.time.Instant;

/**
 * A {@link ClockProvider} that returns the real system time.
 *
 * <p>Phase XI §114: Production clock implementation.</p>
 *
 * @author RegalPine
 */
public final class SystemClockProvider implements ClockProvider {

    @Override
    public Instant now() {
        return Instant.now();
    }
}
