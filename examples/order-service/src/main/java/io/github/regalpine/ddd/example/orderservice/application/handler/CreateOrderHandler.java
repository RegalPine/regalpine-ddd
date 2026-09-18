package io.github.regalpine.ddd.example.orderservice.application.handler;

import io.github.regalpine.ddd.application.command.CommandHandler;
import io.github.regalpine.ddd.core.identifier.Identifier;
import io.github.regalpine.ddd.domain.repository.AggregateRepository;
import io.github.regalpine.ddd.example.orderservice.application.command.CreateOrderCommand;
import io.github.regalpine.ddd.example.orderservice.domain.order.*;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

public class CreateOrderHandler implements CommandHandler<CreateOrderCommand, String> {

    private final AggregateRepository<Order, OrderId> repository;

    public CreateOrderHandler(AggregateRepository<Order, OrderId> repository) {
        this.repository = repository;
    }

    @Override
    public String handle(CreateOrderCommand command) {
        var orderId = new OrderId(UUID.randomUUID().toString());
        var customerId = new CustomerId(command.customerId());

        List<OrderLine> lines = command.lines().stream()
                .map(l -> new OrderLine(
                        l.productId(),
                        l.quantity(),
                        new Money(BigDecimal.valueOf(l.unitPrice()), Currency.getInstance("USD"))))
                .toList();

        Order order = Order.create(orderId, customerId, lines);
        repository.save(order);
        return orderId.value();
    }
}
