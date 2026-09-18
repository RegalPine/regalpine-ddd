package io.github.regalpine.ddd.infrastructure.clock;

import java.time.Instant;
import java.util.Objects;

/**
 * A {@link ClockProvider} that always returns a fixed instant.
 *
 * <p>Phase XI §114/§116: Use in tests for deterministic time.</p>
 *
 * @author RegalPine
 */
public final class FixedClockProvider implements ClockProvider {

    private final Instant fixedInstant;

    /**
     * Creates a fixed clock provider that always returns the given instant.
     *
     * @param fixedInstant the fixed instant, must not be {@code null}
     */
    public FixedClockProvider(Instant fixedInstant) {
        this.fixedInstant = Objects.requireNonNull(fixedInstant, "fixedInstant must not be null");
    }

    @Override
    public Instant now() {
        return fixedInstant;
    }
}
