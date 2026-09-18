package io.github.regalpine.ddd.runtime;

import java.util.Objects;

/**
 * Configuration for the DDD runtime.
 *
 * @author RegalPine
 */
public record RuntimeConfig(
        boolean transactionalCommands,
        boolean publishEventsAfterCommit,
        int outboxPollLimit,
        long outboxPollIntervalMs
) {

    public static final RuntimeConfig DEFAULT = new RuntimeConfig(true, true, 100, 1000L);

    public RuntimeConfig {
        if (outboxPollLimit <= 0) {
            throw new IllegalArgumentException("outboxPollLimit must be positive");
        }
        if (outboxPollIntervalMs < 0) {
            throw new IllegalArgumentException("outboxPollIntervalMs must not be negative");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private boolean transactionalCommands = true;
        private boolean publishEventsAfterCommit = true;
        private int outboxPollLimit = 100;
        private long outboxPollIntervalMs = 1000L;

        public Builder transactionalCommands(boolean value) {
            this.transactionalCommands = value;
            return this;
        }

        public Builder publishEventsAfterCommit(boolean value) {
            this.publishEventsAfterCommit = value;
            return this;
        }

        public Builder outboxPollLimit(int value) {
            this.outboxPollLimit = value;
            return this;
        }

        public Builder outboxPollIntervalMs(long value) {
            this.outboxPollIntervalMs = value;
            return this;
        }

        public RuntimeConfig build() {
            return new RuntimeConfig(transactionalCommands, publishEventsAfterCommit,
                    outboxPollLimit, outboxPollIntervalMs);
        }
    }
}
