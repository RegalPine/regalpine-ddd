package io.github.regalpine.ddd.example.orderservice;

import io.github.regalpine.ddd.example.orderservice.application.command.CreateOrderCommand;
import io.github.regalpine.ddd.example.orderservice.application.command.PayOrderCommand;
import io.github.regalpine.ddd.example.orderservice.application.handler.CreateOrderHandler;
import io.github.regalpine.ddd.example.orderservice.application.handler.GetOrderHandler;
import io.github.regalpine.ddd.example.orderservice.application.handler.PayOrderHandler;
import io.github.regalpine.ddd.example.orderservice.application.query.GetOrderQuery;
import io.github.regalpine.ddd.example.orderservice.bootstrap.OrderServiceBootstrap;
import io.github.regalpine.ddd.example.orderservice.domain.order.Order;
import io.github.regalpine.ddd.example.orderservice.domain.order.OrderId;
import io.github.regalpine.ddd.example.orderservice.domain.order.OrderStatus;
import io.github.regalpine.ddd.infrastructure.persistence.InMemoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OrderFlowTest {

    private InMemoryRepository<Order, OrderId> repo;
    private CreateOrderHandler createHandler;
    private PayOrderHandler payHandler;
    private GetOrderHandler getHandler;

    @BeforeEach
    void setUp() {
        repo = OrderServiceBootstrap.createRepository();
        createHandler = OrderServiceBootstrap.createCreateOrderHandler(repo);
        payHandler = OrderServiceBootstrap.createPayOrderHandler(repo);
        getHandler = OrderServiceBootstrap.createGetOrderHandler(repo);
    }

    @Test
    void createOrderShouldPersistAndEmitEvent() {
        var command = new CreateOrderCommand("cust-1", List.of(
                new CreateOrderCommand.OrderLineInput("product-A", 2, 10.0)));

        String orderId = createHandler.handle(command);

        assertThat(orderId).isNotBlank();
        var order = repo.findById(new OrderId(orderId));
        assertThat(order).isPresent();
        assertThat(order.get().status()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.get().domainEvents()).hasSize(1);
    }

    @Test
    void payOrderShouldTransitionToPaid() {
        var command = new CreateOrderCommand("cust-1", List.of(
                new CreateOrderCommand.OrderLineInput("product-A", 1, 50.0)));
        String orderId = createHandler.handle(command);

        payHandler.handle(new PayOrderCommand(orderId));

        var order = repo.findById(new OrderId(orderId)).orElseThrow();
        assertThat(order.status()).isEqualTo(OrderStatus.PAID);
        assertThat(order.domainEvents()).isNotEmpty();
    }

    @Test
    void getOrderShouldReturnSummary() {
        var command = new CreateOrderCommand("cust-2", List.of(
                new CreateOrderCommand.OrderLineInput("product-B", 3, 20.0)));
        String orderId = createHandler.handle(command);

        Optional<GetOrderQuery.OrderSummary> summary =
                getHandler.handle(new GetOrderQuery(orderId));

        assertThat(summary).isPresent();
        assertThat(summary.get().customerId()).isEqualTo("cust-2");
        assertThat(summary.get().status()).isEqualTo("CREATED");
        assertThat(summary.get().lineCount()).isEqualTo(1);
    }
}
