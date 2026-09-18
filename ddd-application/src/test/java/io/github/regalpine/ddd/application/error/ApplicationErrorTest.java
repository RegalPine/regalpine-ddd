package io.github.regalpine.ddd.application.error;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationErrorTest {

    // -- Test fixtures --

    enum TestAppError implements ApplicationError {
        ORDER_NOT_FOUND("APP_001", "Order not found"),
        DUPLICATE_PAYMENT("APP_002", "Payment already processed");

        private final String code;
        private final String message;

        TestAppError(String code, String message) {
            this.code = code;
            this.message = message;
        }

        @Override
        public String code() {
            return code;
        }

        @Override
        public String message() {
            return message;
        }
    }

    // -- Tests --

    @Test
    void shouldImplementApplicationError() {
        ApplicationError error = TestAppError.ORDER_NOT_FOUND;
        assertThat(error).isInstanceOf(ApplicationError.class);
    }

    @Test
    void shouldReturnErrorCode() {
        assertThat(TestAppError.ORDER_NOT_FOUND.code()).isEqualTo("APP_001");
    }

    @Test
    void shouldReturnErrorMessage() {
        assertThat(TestAppError.DUPLICATE_PAYMENT.message()).isEqualTo("Payment already processed");
    }

    @Test
    void shouldDistinguishFromDomainError() {
        ApplicationError appError = TestAppError.ORDER_NOT_FOUND;
        assertThat(appError.code()).startsWith("APP_");
    }
}
