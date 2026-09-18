package io.github.regalpine.ddd.application.query;

import io.github.regalpine.ddd.core.pagination.PageRequest;
import io.github.regalpine.ddd.core.pagination.PageResult;

/**
 * A query that returns paginated results.
 *
 * <p>Phase XII §20: combines the {@link Query} contract with a
 * {@link PageRequest} to enable framework-level pagination.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * public record ListOrdersQuery(
 *         String customerId,
 *         PageRequest pageRequest
 * ) implements PageQuery<OrderView> {
 * }
 * }</pre>
 *
 * @param <R> the result element type
 * @author RegalPine
 */
public interface PageQuery<R> extends Query<PageResult<R>> {

    /**
     * Returns the pagination request for this query.
     *
     * @return the page request, never {@code null}
     */
    PageRequest pageRequest();
}
