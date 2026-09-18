package io.github.regalpine.ddd.application.query;

import io.github.regalpine.ddd.application.result.ApplicationResult;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class QueryHandlerTest {

    // -- Test fixtures --

    record OrderView(String orderId, String status, String customerName) implements ApplicationResult {}

    record GetOrderQuery(String orderId) implements Query<OrderView> {}

    static final class GetOrderHandler implements QueryHandler<GetOrderQuery, OrderView> {
        @Override
        public OrderView handle(GetOrderQuery query) {
            return new OrderView(query.orderId(), "ACTIVE", "John Doe");
        }
    }

    // -- Tests --

    @Test
    void shouldHandleQueryAndReturnResult() {
        QueryHandler<GetOrderQuery, OrderView> handler = new GetOrderHandler();
        var result = handler.handle(new GetOrderQuery("order-1"));

        assertThat(result.orderId()).isEqualTo("order-1");
        assertThat(result.status()).isEqualTo("ACTIVE");
        assertThat(result.customerName()).isEqualTo("John Doe");
    }

    @Test
    void shouldImplementQueryInterface() {
        GetOrderQuery query = new GetOrderQuery("order-1");
        assertThat(query).isInstanceOf(Query.class);
        assertThat(query.orderId()).isEqualTo("order-1");
    }

    @Test
    void shouldReturnViewAsApplicationResult() {
        OrderView view = new OrderView("order-1", "ACTIVE", "John");
        assertThat(view).isInstanceOf(ApplicationResult.class);
    }
}
