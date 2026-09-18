package io.github.regalpine.ddd.core.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FrameworkExceptionTest {

    @Test
    void frameworkExceptionShouldExtendRuntimeException() {
        var ex = new FrameworkException("test");
        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("test");
    }

    @Test
    void frameworkExceptionShouldSupportCause() {
        var cause = new RuntimeException("cause");
        var ex = new FrameworkException("msg", cause);
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void dddExceptionShouldExtendFrameworkException() {
        var ex = new DddException("ddd");
        assertThat(ex).isInstanceOf(FrameworkException.class);
    }

    @Test
    void transactionExceptionShouldExtendFrameworkException() {
        var ex = new TransactionException("tx");
        assertThat(ex).isInstanceOf(FrameworkException.class);
    }

    @Test
    void messagingExceptionShouldExtendFrameworkException() {
        var ex = new MessagingException("msg");
        assertThat(ex).isInstanceOf(FrameworkException.class);
    }
}
