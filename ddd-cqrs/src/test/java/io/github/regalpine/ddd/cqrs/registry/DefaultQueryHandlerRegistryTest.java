package io.github.regalpine.ddd.cqrs.registry;

import io.github.regalpine.ddd.application.query.Query;
import io.github.regalpine.ddd.application.query.QueryHandler;
import io.github.regalpine.ddd.cqrs.bus.DuplicateHandlerException;
import io.github.regalpine.ddd.cqrs.bus.HandlerNotFoundException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DefaultQueryHandlerRegistryTest {

    record GetOrder(String orderId) implements Query<String> {}

    static class GetOrderHandler implements QueryHandler<GetOrder, String> {
        @Override
        public String handle(GetOrder query) {
            return "order:" + query.orderId();
        }
    }

    @Test
    void registerAndFindHandler() {
        var registry = new DefaultQueryHandlerRegistry();
        var handler = new GetOrderHandler();
        registry.register(handler);

        QueryHandler<GetOrder, String> found = registry.find(GetOrder.class);
        assertThat(found).isSameAs(handler);
    }

    @Test
    void findShouldThrowWhenNotRegistered() {
        var registry = new DefaultQueryHandlerRegistry();

        assertThatThrownBy(() -> registry.find(GetOrder.class))
                .isInstanceOf(HandlerNotFoundException.class);
    }

    @Test
    void registerShouldThrowOnDuplicate() {
        var registry = new DefaultQueryHandlerRegistry();
        registry.register(new GetOrderHandler());

        assertThatThrownBy(() -> registry.register(new GetOrderHandler()))
                .isInstanceOf(DuplicateHandlerException.class);
    }
}
