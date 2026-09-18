package io.github.regalpine.ddd.messaging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MessageStateTest {

    @Test
    void shouldHaveAllLifecycleStates() {
        assertThat(MessageState.values()).containsExactly(
                MessageState.CREATED,
                MessageState.OUTBOXED,
                MessageState.PUBLISHED,
                MessageState.RECEIVED,
                MessageState.PROCESSING,
                MessageState.PROCESSED,
                MessageState.RETRYING,
                MessageState.DEAD_LETTERED
        );
    }

    @Test
    void shouldHaveExpectedCount() {
        assertThat(MessageState.values()).hasSize(8);
    }
}
