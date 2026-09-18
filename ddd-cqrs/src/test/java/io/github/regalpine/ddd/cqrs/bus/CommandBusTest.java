package io.github.regalpine.ddd.cqrs.bus;

import io.github.regalpine.ddd.application.command.Command;
import io.github.regalpine.ddd.application.command.CommandHandler;
import io.github.regalpine.ddd.cqrs.registry.DefaultCommandHandlerRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CommandBusTest {

    record TestCommand(String value) implements Command<String> {}

    static class TestHandler implements CommandHandler<TestCommand, String> {
        @Override
        public String handle(TestCommand command) {
            return "handled:" + command.value();
        }
    }

    @Test
    void dispatchShouldRouteToRegisteredHandler() {
        var registry = new DefaultCommandHandlerRegistry();
        registry.register(new TestHandler());
        CommandBus bus = new DefaultCommandBus(registry);

        String result = bus.dispatch(new TestCommand("hello"));

        assertThat(result).isEqualTo("handled:hello");
    }

    @Test
    void dispatchShouldThrowWhenNoHandlerRegistered() {
        var registry = new DefaultCommandHandlerRegistry();
        CommandBus bus = new DefaultCommandBus(registry);

        assertThatThrownBy(() -> bus.dispatch(new TestCommand("x")))
                .isInstanceOf(HandlerNotFoundException.class);
    }

    @Test
    void dispatchShouldRejectNullCommand() {
        var registry = new DefaultCommandHandlerRegistry();
        CommandBus bus = new DefaultCommandBus(registry);

        assertThatThrownBy(() -> bus.dispatch(null))
                .isInstanceOf(NullPointerException.class);
    }
}
