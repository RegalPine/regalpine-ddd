package io.github.regalpine.ddd.example.orderservice.application.command;

import io.github.regalpine.ddd.application.command.Command;

public record CancelOrderCommand(String orderId, String reason) implements Command<Void> {
}
