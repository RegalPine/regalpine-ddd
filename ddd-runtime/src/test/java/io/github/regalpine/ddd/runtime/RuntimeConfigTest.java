package io.github.regalpine.ddd.runtime;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link RuntimeConfig} record.
 */
class RuntimeConfigTest {

    @Test
    void shouldProvideDefaultValues() {
        RuntimeConfig config = RuntimeConfig.DEFAULT;

        assertThat(config.transactionalCommands()).isTrue();
        assertThat(config.publishEventsAfterCommit()).isTrue();
        assertThat(config.outboxPollLimit()).isEqualTo(100);
        assertThat(config.outboxPollIntervalMs()).isEqualTo(1000L);
    }

    @Test
    void shouldBuildCustomConfig() {
        RuntimeConfig config = RuntimeConfig.builder()
                .transactionalCommands(false)
                .outboxPollLimit(50)
                .outboxPollIntervalMs(2000L)
                .build();

        assertThat(config.transactionalCommands()).isFalse();
        assertThat(config.outboxPollLimit()).isEqualTo(50);
        assertThat(config.outboxPollIntervalMs()).isEqualTo(2000L);
    }

    @Test
    void shouldRejectInvalidValues() {
        assertThatThrownBy(() -> new RuntimeConfig(true, true, 0, 1000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("outboxPollLimit");

        assertThatThrownBy(() -> new RuntimeConfig(true, true, 100, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("outboxPollIntervalMs");
    }
}
