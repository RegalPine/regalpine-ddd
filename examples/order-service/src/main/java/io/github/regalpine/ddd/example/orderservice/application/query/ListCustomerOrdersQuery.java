package io.github.regalpine.ddd.example.orderservice.application.query;

import io.github.regalpine.ddd.application.query.Query;

import java.util.List;

public record ListCustomerOrdersQuery(String customerId) implements Query<List<String>> {
}
