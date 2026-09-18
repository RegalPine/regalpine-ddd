package io.github.regalpine.ddd.example.orderservice.domain.order;

public record OrderLine(String productId, int quantity, Money unitPrice) {
    public OrderLine {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (unitPrice == null) {
            throw new IllegalArgumentException("unitPrice must not be null");
        }
    }

    public Money subtotal() {
        return new Money(unitPrice.amount().multiply(java.math.BigDecimal.valueOf(quantity)), unitPrice.currency());
    }
}
