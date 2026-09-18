package io.github.regalpine.ddd.example.orderservice.application.command;

import io.github.regalpine.ddd.application.command.Command;

public record PayOrderCommand(String orderId) implements Command<Void> {
}
