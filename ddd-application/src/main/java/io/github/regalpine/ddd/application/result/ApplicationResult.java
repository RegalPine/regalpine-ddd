package io.github.regalpine.ddd.application.result;

/**
 * Marker interface for application-level results.
 *
 * <p>Concrete result types should be implemented as Java records
 * that implement this interface. This avoids returning raw
 * {@code Object} from handlers while keeping the framework
 * open to any result shape.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * public record PayOrderResult(OrderId orderId)
 *         implements ApplicationResult {
 * }
 * }</pre>
 *
 * <p>Application results must not expose infrastructure details
 * such as JPA entities, Hibernate proxies, or database rows.</p>
 *
 * @see io.github.regalpine.ddd.application.command.CommandHandler
 * @see io.github.regalpine.ddd.application.query.QueryHandler
 */
public interface ApplicationResult {
}
