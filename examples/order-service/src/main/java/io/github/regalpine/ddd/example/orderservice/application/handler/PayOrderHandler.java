package io.github.regalpine.ddd.example.orderservice.application.handler;

import io.github.regalpine.ddd.application.command.CommandHandler;
import io.github.regalpine.ddd.domain.repository.AggregateRepository;
import io.github.regalpine.ddd.example.orderservice.application.command.PayOrderCommand;
import io.github.regalpine.ddd.example.orderservice.domain.order.Order;
import io.github.regalpine.ddd.example.orderservice.domain.order.OrderId;

public class PayOrderHandler implements CommandHandler<PayOrderCommand, Void> {

    private final AggregateRepository<Order, OrderId> repository;

    public PayOrderHandler(AggregateRepository<Order, OrderId> repository) {
        this.repository = repository;
    }

    @Override
    public Void handle(PayOrderCommand command) {
        var orderId = new OrderId(command.orderId());
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + command.orderId()));
        order.pay();
        repository.save(order);
        return null;
    }
}
