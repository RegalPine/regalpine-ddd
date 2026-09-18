package io.github.regalpine.ddd.core.exception;

import io.github.regalpine.ddd.core.error.DomainError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainExceptionTest {

    private static final DomainError TEST_ERROR = new DomainError() {
        @Override
        public String code() {
            return "TEST_001";
        }

        @Override
        public String message() {
            return "Test error message";
        }
    };

    @Test
    void shouldCreateExceptionWithError() {
        var exception = new DomainException(TEST_ERROR);
        assertThat(exception.error()).isSameAs(TEST_ERROR);
        assertThat(exception.getMessage()).isEqualTo("Test error message");
    }

    @Test
    void shouldCreateExceptionWithErrorAndCause() {
        var cause = new RuntimeException("root cause");
        var exception = new DomainException(TEST_ERROR, cause);
        assertThat(exception.error()).isSameAs(TEST_ERROR);
        assertThat(exception.getCause()).isSameAs(cause);
    }

    @Test
    void shouldRejectNullError() {
        assertThatThrownBy(() -> new DomainException(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    void shouldBeRuntimeException() {
        var exception = new DomainException(TEST_ERROR);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
