package io.github.regalpine.ddd.cqrs.bus;

import io.github.regalpine.ddd.application.query.Query;
import io.github.regalpine.ddd.application.query.QueryHandler;
import io.github.regalpine.ddd.cqrs.registry.DefaultQueryHandlerRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class QueryBusTest {

    record TestQuery(String key) implements Query<Integer> {}

    static class TestHandler implements QueryHandler<TestQuery, Integer> {
        @Override
        public Integer handle(TestQuery query) {
            return query.key().length();
        }
    }

    @Test
    void dispatchShouldRouteToRegisteredHandler() {
        var registry = new DefaultQueryHandlerRegistry();
        registry.register(new TestHandler());
        QueryBus bus = new DefaultQueryBus(registry);

        Integer result = bus.dispatch(new TestQuery("hello"));

        assertThat(result).isEqualTo(5);
    }

    @Test
    void dispatchShouldThrowWhenNoHandlerRegistered() {
        var registry = new DefaultQueryHandlerRegistry();
        QueryBus bus = new DefaultQueryBus(registry);

        assertThatThrownBy(() -> bus.dispatch(new TestQuery("x")))
                .isInstanceOf(HandlerNotFoundException.class);
    }

    @Test
    void dispatchShouldRejectNullQuery() {
        var registry = new DefaultQueryHandlerRegistry();
        QueryBus bus = new DefaultQueryBus(registry);

        assertThatThrownBy(() -> bus.dispatch(null))
                .isInstanceOf(NullPointerException.class);
    }
}
