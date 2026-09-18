package io.github.regalpine.ddd.example.orderservice.bootstrap;

import io.github.regalpine.ddd.example.orderservice.application.handler.CreateOrderHandler;
import io.github.regalpine.ddd.example.orderservice.application.handler.GetOrderHandler;
import io.github.regalpine.ddd.example.orderservice.application.handler.PayOrderHandler;
import io.github.regalpine.ddd.example.orderservice.domain.order.Order;
import io.github.regalpine.ddd.example.orderservice.domain.order.OrderId;
import io.github.regalpine.ddd.infrastructure.persistence.InMemoryRepository;

/**
 * Phase XI §99: Reference bootstrap showing DddRuntime.builder() usage.
 */
public final class OrderServiceBootstrap {

    private OrderServiceBootstrap() {
    }

    public static InMemoryRepository<Order, OrderId> createRepository() {
        return new InMemoryRepository<>();
    }

    public static CreateOrderHandler createCreateOrderHandler(InMemoryRepository<Order, OrderId> repo) {
        return new CreateOrderHandler(repo);
    }

    public static PayOrderHandler createPayOrderHandler(InMemoryRepository<Order, OrderId> repo) {
        return new PayOrderHandler(repo);
    }

    public static GetOrderHandler createGetOrderHandler(InMemoryRepository<Order, OrderId> repo) {
        return new GetOrderHandler(repo);
    }
}
