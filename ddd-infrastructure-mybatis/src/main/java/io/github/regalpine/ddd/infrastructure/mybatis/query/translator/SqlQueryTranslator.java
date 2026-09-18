package io.github.regalpine.ddd.infrastructure.mybatis.query.translator;

import io.github.regalpine.ddd.infrastructure.mybatis.query.ast.*;
import io.github.regalpine.ddd.infrastructure.mybatis.query.ast.LogicalNode.LogicalOperator;
import io.github.regalpine.ddd.infrastructure.mybatis.query.ast.OrderNode.Direction;
import io.github.regalpine.ddd.infrastructure.mybatis.query.parameter.ParameterBinding;
import io.github.regalpine.ddd.infrastructure.mybatis.query.pagination.OffsetPagination;
import io.github.regalpine.ddd.infrastructure.mybatis.query.pagination.Pagination;
import io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper.AbstractQueryWrapper;
import io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper.QueryWrapper;
import io.github.regalpine.ddd.infrastructure.mybatis.pagination.PaginationDialect;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Default SQL implementation of {@link QueryTranslator}.
 *
 * <p>Phase XIII §29/§31/§32: walks the AST produced by the {@link QueryWrapper}
 * and generates a SQL fragment with {@link ParameterBinding} instances.
 * All values go through prepared parameters — direct SQL concatenation is forbidden (§31).</p>
 *
 * <p>Example output:</p>
 * <pre>
 * customer_id = #{p1} AND status = #{p2} ORDER BY created_at DESC
 * </pre>
 *
 * @author RegalPine
 */
public class SqlQueryTranslator implements QueryTranslator {

    private final PaginationDialect dialect;
    private final LikeEscaper likeEscaper;

    public SqlQueryTranslator(PaginationDialect dialect, LikeEscaper likeEscaper) {
        this.dialect = dialect;
        this.likeEscaper = likeEscaper;
    }

    public SqlQueryTranslator() {
        this(null, new DefaultLikeEscaper());
    }

    @Override
    public TranslationResult translate(QueryWrapper<?> wrapper) {
        var ctx = new TranslationContext(dialect);
        var sb = new StringBuilder();

        List<QueryNode> nodes = wrapper.nodes();

        // Separate condition nodes, order nodes, select nodes, pagination nodes
        var conditions = nodes.stream()
                .filter(n -> n instanceof ConditionNode || n instanceof LogicalNode || n instanceof NestedNode)
                .toList();
        var orders = nodes.stream()
                .filter(OrderNode.class::isInstance)
                .map(OrderNode.class::cast)
                .toList();
        var selects = nodes.stream()
                .filter(SelectNode.class::isInstance)
                .map(SelectNode.class::cast)
                .toList();
        var paginationNodes = nodes.stream()
                .filter(PaginationNode.class::isInstance)
                .map(PaginationNode.class::cast)
                .toList();

        // SELECT clause
        if (!selects.isEmpty()) {
            var selectNode = selects.get(0);
            sb.append("SELECT ");
            sb.append(selectNode.fields().stream()
                    .map(f -> f.column())
                    .collect(Collectors.joining(", ")));
            sb.append(" ");
        }

        // WHERE clause
        if (!conditions.isEmpty()) {
            sb.append("WHERE ");
            for (int i = 0; i < conditions.size(); i++) {
                if (i > 0) {
                    sb.append(" AND ");
                }
                translateNode(conditions.get(i), sb, ctx);
            }
        }

        // ORDER BY clause
        if (!orders.isEmpty()) {
            sb.append("ORDER BY ");
            for (int i = 0; i < orders.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                var order = orders.get(i);
                sb.append(order.field().column());
                sb.append(order.direction() == Direction.ASC ? " ASC" : " DESC");
            }
        }

        // Pagination clause
        if (!paginationNodes.isEmpty()) {
            var pagNode = paginationNodes.get(0);
            Pagination pag = pagNode.pagination();
            if (pag instanceof OffsetPagination offsetPag && dialect != null) {
                String baseSql = sb.toString().trim();
                String paginatedSql = dialect.applyOffsetLimit(
                        baseSql, offsetPag.offset(), offsetPag.size());
                sb = new StringBuilder(paginatedSql);
            }
        }

        return new TranslationResult(sb.toString().trim(), ctx.bindings().toList());
    }

    private void translateNode(QueryNode node, StringBuilder sb, TranslationContext ctx) {
        if (node instanceof ComparisonNode<?> cmp) {
            translateComparison(cmp, sb, ctx);
        } else if (node instanceof NullNode nul) {
            translateNull(nul, sb);
        } else if (node instanceof InNode<?> inNode) {
            translateIn(inNode, sb, ctx);
        } else if (node instanceof BetweenNode<?> between) {
            translateBetween(between, sb, ctx);
        } else if (node instanceof LikeNode like) {
            translateLike(like, sb, ctx);
        } else if (node instanceof LogicalNode logical) {
            translateLogical(logical, sb, ctx);
        } else if (node instanceof NestedNode nested) {
            translateNested(nested, sb, ctx);
        }
    }

    private void translateComparison(ComparisonNode<?> node, StringBuilder sb, TranslationContext ctx) {
        sb.append(node.field().column());
        sb.append(' ').append(operatorSql(node.operator())).append(' ');

        // §14: empty IN sentinel → FALSE
        if (node.value() == AbstractQueryWrapper.EMPTY_IN_FALSE) {
            sb.append("1 = 0");
            return;
        }

        var binding = ctx.bindings().add(node.value(), node.field().javaType());
        sb.append("#{").append(binding.name()).append("}");
    }

    private void translateNull(NullNode node, StringBuilder sb) {
        sb.append(node.field().column());
        if (node.negated()) {
            sb.append(" IS NOT NULL");
        } else {
            sb.append(" IS NULL");
        }
    }

    private void translateIn(InNode<?> node, StringBuilder sb, TranslationContext ctx) {
        sb.append(node.field().column());
        if (node.negated()) {
            sb.append(" NOT");
        }
        sb.append(" IN (");
        for (int i = 0; i < node.values().size(); i++) {
            if (i > 0) sb.append(", ");
            var binding = ctx.bindings().add(node.values().get(i), node.field().javaType());
            sb.append("#{").append(binding.name()).append("}");
        }
        sb.append(")");
    }

    private void translateBetween(BetweenNode<?> node, StringBuilder sb, TranslationContext ctx) {
        sb.append(node.field().column());
        if (node.negated()) {
            sb.append(" NOT");
        }
        sb.append(" BETWEEN ");
        var lowerBinding = ctx.bindings().add(node.lower(), node.field().javaType());
        var upperBinding = ctx.bindings().add(node.upper(), node.field().javaType());
        sb.append("#{").append(lowerBinding.name()).append("}");
        sb.append(" AND ");
        sb.append("#{").append(upperBinding.name()).append("}");
    }

    private void translateLike(LikeNode node, StringBuilder sb, TranslationContext ctx) {
        sb.append(node.field().column());
        if (node.negated()) {
            sb.append(" NOT");
        }
        sb.append(" LIKE ");

        String escaped = likeEscaper.escape(node.value());
        String pattern = switch (node.mode()) {
            case ANYWHERE -> "%" + escaped + "%";
            case START -> escaped + "%";
            case END -> "%" + escaped;
        };

        var binding = ctx.bindings().add(pattern, String.class);
        sb.append("#{").append(binding.name()).append("}");
    }

    private void translateLogical(LogicalNode node, StringBuilder sb, TranslationContext ctx) {
        String joiner = node.operator() == LogicalOperator.AND ? " AND " : " OR ";
        for (int i = 0; i < node.children().size(); i++) {
            if (i > 0) sb.append(joiner);
            translateNode(node.children().get(i), sb, ctx);
        }
    }

    private void translateNested(NestedNode node, StringBuilder sb, TranslationContext ctx) {
        sb.append("(");
        String joiner = node.operator() == LogicalOperator.AND ? " AND " : " OR ";
        for (int i = 0; i < node.conditions().size(); i++) {
            if (i > 0) sb.append(joiner);
            translateNode(node.conditions().get(i), sb, ctx);
        }
        sb.append(")");
    }

    private String operatorSql(ComparisonNode.Operator op) {
        return switch (op) {
            case EQ -> "=";
            case NE -> "!=";
            case GT -> ">";
            case GE -> ">=";
            case LT -> "<";
            case LE -> "<=";
        };
    }

    /**
     * LIKE escape interface for the translator.
     */
    public interface LikeEscaper {
        String escape(String value);
    }

    /**
     * Default LIKE escaper that escapes %, _, and \ characters.
     */
    public static class DefaultLikeEscaper implements LikeEscaper {
        @Override
        public String escape(String value) {
            if (value == null) return "";
            return value
                    .replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_");
        }
    }
}
