package io.github.regalpine.ddd.cqrs.registry;

import io.github.regalpine.ddd.application.command.Command;
import io.github.regalpine.ddd.application.command.CommandHandler;
import io.github.regalpine.ddd.cqrs.bus.DuplicateHandlerException;
import io.github.regalpine.ddd.cqrs.bus.HandlerNotFoundException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DefaultCommandHandlerRegistryTest {

    record CreateOrder(String orderId) implements Command<String> {}

    static class CreateOrderHandler implements CommandHandler<CreateOrder, String> {
        @Override
        public String handle(CreateOrder command) {
            return command.orderId();
        }
    }

    static class DuplicateCreateOrderHandler implements CommandHandler<CreateOrder, String> {
        @Override
        public String handle(CreateOrder command) {
            return "duplicate";
        }
    }

    @Test
    void registerAndFindHandler() {
        var registry = new DefaultCommandHandlerRegistry();
        var handler = new CreateOrderHandler();
        registry.register(handler);

        CommandHandler<CreateOrder, String> found = registry.find(CreateOrder.class);
        assertThat(found).isSameAs(handler);
    }

    @Test
    void findShouldThrowWhenNotRegistered() {
        var registry = new DefaultCommandHandlerRegistry();

        assertThatThrownBy(() -> registry.find(CreateOrder.class))
                .isInstanceOf(HandlerNotFoundException.class);
    }

    @Test
    void registerShouldThrowOnDuplicate() {
        var registry = new DefaultCommandHandlerRegistry();
        registry.register(new CreateOrderHandler());

        assertThatThrownBy(() -> registry.register(new CreateOrderHandler()))
                .isInstanceOf(DuplicateHandlerException.class);
    }
}
