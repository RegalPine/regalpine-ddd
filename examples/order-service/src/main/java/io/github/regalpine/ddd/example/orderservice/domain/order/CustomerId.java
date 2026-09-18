package io.github.regalpine.ddd.example.orderservice.domain.order;

import io.github.regalpine.ddd.core.identifier.Identifier;

public record CustomerId(String value) implements Identifier {
    public CustomerId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CustomerId must not be blank");
        }
    }
}
