package io.github.regalpine.ddd.example.orderservice.domain.order;

import io.github.regalpine.ddd.core.identifier.Identifier;

public record OrderId(String value) implements Identifier {
    public OrderId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("OrderId must not be blank");
        }
    }
}
