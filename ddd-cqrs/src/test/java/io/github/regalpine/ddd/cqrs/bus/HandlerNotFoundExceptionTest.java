package io.github.regalpine.ddd.cqrs.bus;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class HandlerNotFoundExceptionTest {

    @Test
    void shouldContainMessageType() {
        var ex = new HandlerNotFoundException("com.example.MyQuery");

        assertThat(ex.getMessage()).contains("com.example.MyQuery");
    }

    @Test
    void shouldBeApplicationException() {
        var ex = new HandlerNotFoundException("test");

        assertThat(ex).isInstanceOf(io.github.regalpine.ddd.core.exception.ApplicationException.class);
    }
}
