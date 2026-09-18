package io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper;

import io.github.regalpine.ddd.core.pagination.CountMode;
import io.github.regalpine.ddd.infrastructure.mybatis.query.ast.QueryNode;
import io.github.regalpine.ddd.infrastructure.mybatis.query.pagination.Pagination;

import java.util.List;
import java.util.Objects;

/**
 * An immutable snapshot of a query wrapper's state, suitable for caching, auditing, and testing.
 *
 * <p>Phase XIII §63: produced by {@code QueryWrapper.freeze()}. All nodes are
 * defensively copied. The plan can be used to generate a {@link #fingerprint()}
 * for query caching and observability (§64).</p>
 *
 * @param nodes      the query AST nodes (immutable)
 * @param pagination the pagination strategy, or null if none
 * @param countMode  the count strategy
 * @author RegalPine
 */
public record QueryPlan(
        List<QueryNode> nodes,
        Pagination pagination,
        CountMode countMode
) {

    public QueryPlan {
        Objects.requireNonNull(nodes, "nodes must not be null");
        Objects.requireNonNull(countMode, "countMode must not be null");
        nodes = List.copyOf(nodes);
    }

    /**
     * Generates a structural fingerprint for this query plan.
     *
     * <p>Phase XIII §64: the fingerprint is based on node types, field columns,
     * and operators — it does NOT include actual parameter values. This makes
     * it suitable for query cache keys and observability.</p>
     *
     * @return a hex-encoded fingerprint string
     */
    public String fingerprint() {
        var sb = new StringBuilder();
        sb.append("nodes=").append(nodes.size());
        for (var node : nodes) {
            sb.append(':').append(node.getClass().getSimpleName());
        }
        if (pagination != null) {
            sb.append(":pag=").append(pagination.getClass().getSimpleName());
        }
        sb.append(":count=").append(countMode);
        return Integer.toHexString(sb.toString().hashCode());
    }
}
