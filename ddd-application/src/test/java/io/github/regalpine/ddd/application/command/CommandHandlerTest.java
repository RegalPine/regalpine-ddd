package io.github.regalpine.ddd.application.command;

import io.github.regalpine.ddd.application.result.ApplicationResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommandHandlerTest {

    // -- Test fixtures --

    record PayOrderResult(String orderId) implements ApplicationResult {}

    record PayOrderCommand(String orderId, String paymentId) implements Command<PayOrderResult> {}

    static final class PayOrderHandler implements CommandHandler<PayOrderCommand, PayOrderResult> {
        @Override
        public PayOrderResult handle(PayOrderCommand command) {
            return new PayOrderResult(command.orderId());
        }
    }

    // -- Tests --

    @Test
    void shouldHandleCommandAndReturnResult() {
        CommandHandler<PayOrderCommand, PayOrderResult> handler = new PayOrderHandler();
        var result = handler.handle(new PayOrderCommand("order-1", "payment-1"));

        assertThat(result.orderId()).isEqualTo("order-1");
    }

    @Test
    void shouldImplementApplicationResult() {
        PayOrderResult result = new PayOrderResult("order-1");
        assertThat(result).isInstanceOf(ApplicationResult.class);
    }

    @Test
    void shouldImplementCommandInterface() {
        PayOrderCommand command = new PayOrderCommand("order-1", "payment-1");
        assertThat(command).isInstanceOf(Command.class);
        assertThat(command.orderId()).isEqualTo("order-1");
        assertThat(command.paymentId()).isEqualTo("payment-1");
    }
}
