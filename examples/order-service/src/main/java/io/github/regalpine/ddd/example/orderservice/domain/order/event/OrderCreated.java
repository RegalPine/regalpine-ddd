package io.github.regalpine.ddd.example.orderservice.domain.order.event;

import io.github.regalpine.ddd.core.event.*;
import io.github.regalpine.ddd.core.identifier.Identifier;
import io.github.regalpine.ddd.core.version.Version;

import java.time.Instant;

public record OrderCreated(
        EventId eventId,
        AggregateType aggregateType,
        Identifier aggregateId,
        Version aggregateVersion,
        Instant occurredAt,
        String customerId,
        int lineCount
) implements DomainEvent {
}
