package io.github.regalpine.ddd.example.orderservice.application.command;

import io.github.regalpine.ddd.application.command.Command;

public record ShipOrderCommand(String orderId, String trackingNumber) implements Command<Void> {
}
