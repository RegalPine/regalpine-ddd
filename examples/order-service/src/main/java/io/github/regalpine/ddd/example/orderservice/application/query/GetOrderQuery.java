package io.github.regalpine.ddd.example.orderservice.application.query;

import io.github.regalpine.ddd.application.query.Query;

import java.util.Optional;

public record GetOrderQuery(String orderId) implements Query<Optional<GetOrderQuery.OrderSummary>> {

    public record OrderSummary(String orderId, String customerId, String status, int lineCount) {
    }
}
