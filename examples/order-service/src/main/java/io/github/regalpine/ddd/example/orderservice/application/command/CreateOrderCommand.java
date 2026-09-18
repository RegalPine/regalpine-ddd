package io.github.regalpine.ddd.example.orderservice.application.command;

import io.github.regalpine.ddd.application.command.Command;

import java.util.List;

public record CreateOrderCommand(String customerId, List<OrderLineInput> lines) implements Command<String> {

    public record OrderLineInput(String productId, int quantity, double unitPrice) {
    }
}
