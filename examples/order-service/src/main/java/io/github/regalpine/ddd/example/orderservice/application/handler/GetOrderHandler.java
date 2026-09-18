package io.github.regalpine.ddd.example.orderservice.application.handler;

import io.github.regalpine.ddd.application.query.QueryHandler;
import io.github.regalpine.ddd.domain.repository.AggregateRepository;
import io.github.regalpine.ddd.example.orderservice.application.query.GetOrderQuery;
import io.github.regalpine.ddd.example.orderservice.domain.order.Order;
import io.github.regalpine.ddd.example.orderservice.domain.order.OrderId;

import java.util.Optional;

public class GetOrderHandler implements QueryHandler<GetOrderQuery, Optional<GetOrderQuery.OrderSummary>> {

    private final AggregateRepository<Order, OrderId> repository;

    public GetOrderHandler(AggregateRepository<Order, OrderId> repository) {
        this.repository = repository;
    }

    @Override
    public Optional<GetOrderQuery.OrderSummary> handle(GetOrderQuery query) {
        return repository.findById(new OrderId(query.orderId()))
                .map(o -> new GetOrderQuery.OrderSummary(
                        o.id().value(),
                        o.customerId().value(),
                        o.status().name(),
                        o.lines().size()));
    }
}
