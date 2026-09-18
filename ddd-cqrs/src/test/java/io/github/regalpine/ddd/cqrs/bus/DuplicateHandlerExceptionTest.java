package io.github.regalpine.ddd.cqrs.bus;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DuplicateHandlerExceptionTest {

    @Test
    void shouldContainMessageType() {
        var ex = new DuplicateHandlerException("com.example.MyCommand");

        assertThat(ex.getMessage()).contains("com.example.MyCommand");
    }

    @Test
    void shouldBeApplicationException() {
        var ex = new DuplicateHandlerException("test");

        assertThat(ex).isInstanceOf(io.github.regalpine.ddd.core.exception.ApplicationException.class);
    }
}
