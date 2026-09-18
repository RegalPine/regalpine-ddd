package io.github.regalpine.ddd.messaging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MessageHandlingResultTest {

    @Test
    void shouldHaveAllResultValues() {
        assertThat(MessageHandlingResult.values()).containsExactly(
                MessageHandlingResult.SUCCESS,
                MessageHandlingResult.RETRY,
                MessageHandlingResult.DEAD_LETTER,
                MessageHandlingResult.DROP
        );
    }
}
