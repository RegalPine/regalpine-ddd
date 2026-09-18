package io.github.regalpine.ddd.example.orderservice.domain.order;

import io.github.regalpine.ddd.core.aggregate.AggregateRootSupport;
import io.github.regalpine.ddd.core.event.AggregateType;
import io.github.regalpine.ddd.core.event.EventId;
import io.github.regalpine.ddd.core.version.Version;
import io.github.regalpine.ddd.example.orderservice.domain.order.event.OrderCancelled;
import io.github.regalpine.ddd.example.orderservice.domain.order.event.OrderCreated;
import io.github.regalpine.ddd.example.orderservice.domain.order.event.OrderPaid;
import io.github.regalpine.ddd.example.orderservice.domain.order.event.OrderShipped;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Phase XI §90: Order aggregate root.
 */
public final class Order extends AggregateRootSupport<OrderId> {

    private final OrderId orderId;
    private final CustomerId customerId;
    private OrderStatus status;
    private final List<OrderLine> lines;
    private Version version;

    private Order(OrderId orderId, CustomerId customerId, List<OrderLine> lines) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.lines = List.copyOf(lines);
        this.status = OrderStatus.CREATED;
        this.version = Version.initial();
    }

    public static Order create(OrderId orderId, CustomerId customerId, List<OrderLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one line");
        }
        var order = new Order(orderId, customerId, lines);
        order.raise(new OrderCreated(
                new EventId(UUID.randomUUID().toString()),
                new AggregateType("Order"),
                orderId,
                order.version,
                Instant.now(),
                customerId.value(),
                lines.size()
        ));
        return order;
    }

    public void pay() {
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException("Only CREATED orders can be paid, current: " + status);
        }
        this.status = OrderStatus.PAID;
        raise(new OrderPaid(
                new EventId(UUID.randomUUID().toString()),
                new AggregateType("Order"),
                orderId,
                version,
                Instant.now()
        ));
    }

    public void cancel(String reason) {
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException("Only CREATED orders can be cancelled, current: " + status);
        }
        this.status = OrderStatus.CANCELLED;
        raise(new OrderCancelled(
                new EventId(UUID.randomUUID().toString()),
                new AggregateType("Order"),
                orderId,
                version,
                Instant.now(),
                reason
        ));
    }

    public void ship(String trackingNumber) {
        if (status != OrderStatus.PAID) {
            throw new IllegalStateException("Only PAID orders can be shipped, current: " + status);
        }
        this.status = OrderStatus.SHIPPED;
        raise(new OrderShipped(
                new EventId(UUID.randomUUID().toString()),
                new AggregateType("Order"),
                orderId,
                version,
                Instant.now(),
                trackingNumber
        ));
    }

    @Override
    public OrderId id() {
        return orderId;
    }

    public CustomerId customerId() {
        return customerId;
    }

    public OrderStatus status() {
        return status;
    }

    public List<OrderLine> lines() {
        return lines;
    }

    @Override
    public Version version() {
        return version;
    }

    public Money totalAmount() {
        Money total = lines.get(0).subtotal();
        for (int i = 1; i < lines.size(); i++) {
            total = total.add(lines.get(i).subtotal());
        }
        return total;
    }
}
