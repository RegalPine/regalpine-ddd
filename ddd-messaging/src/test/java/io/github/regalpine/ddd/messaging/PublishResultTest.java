package io.github.regalpine.ddd.messaging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PublishResultTest {

    @Test
    void successShouldCreateSuccessfulResult() {
        PublishResult result = PublishResult.success("m1");

        assertThat(result.success()).isTrue();
        assertThat(result.messageId()).isEqualTo("m1");
        assertThat(result.failureReason()).isNull();
    }

    @Test
    void failureShouldCreateFailedResult() {
        PublishResult result = PublishResult.failure("m1", "broker unavailable");

        assertThat(result.success()).isFalse();
        assertThat(result.messageId()).isEqualTo("m1");
        assertThat(result.failureReason()).isEqualTo("broker unavailable");
    }
}
