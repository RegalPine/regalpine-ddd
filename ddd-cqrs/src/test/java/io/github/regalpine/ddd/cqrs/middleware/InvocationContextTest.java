package io.github.regalpine.ddd.cqrs.middleware;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class InvocationContextTest {

    @Test
    void shouldExposeMessageAndType() {
        var ctx = new InvocationContext<String>("hello", "java.lang.String");

        assertThat(ctx.message()).isEqualTo("hello");
        assertThat(ctx.messageType()).isEqualTo("java.lang.String");
    }

    @Test
    void shouldRejectNullMessage() {
        assertThatThrownBy(() -> new InvocationContext<>(null, "type"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("message");
    }

    @Test
    void shouldRejectNullMessageType() {
        assertThatThrownBy(() -> new InvocationContext<>("msg", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("messageType");
    }
}
