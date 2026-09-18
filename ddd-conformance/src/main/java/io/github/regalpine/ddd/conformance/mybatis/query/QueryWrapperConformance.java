package io.github.regalpine.ddd.conformance.mybatis.query;

import io.github.regalpine.ddd.infrastructure.mybatis.query.ast.*;
import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryField;
import io.github.regalpine.ddd.infrastructure.mybatis.query.field.QueryFields;
import io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper.QueryWrapper;
import io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper.Wrappers;

import java.util.List;

/**
 * Phase XIII §76: structural conformance for QueryWrapper API.
 *
 * <p>Verifies that the QueryWrapper implementation provides all required
 * chain methods, produces correct AST nodes, and maintains immutability.</p>
 *
 * @author RegalPine
 */
public final class QueryWrapperConformance {

    private QueryWrapperConformance() {
    }

    /**
     * WRAPPER-001: Wrapper belongs to Query Infrastructure only.
     */
    public static boolean wrapperIsInfrastructure() {
        String pkg = QueryWrapper.class.getPackageName();
        return pkg.contains("infrastructure.mybatis");
    }

    /**
     * WRAPPER-002: Wrapper does not enter Domain Model.
     */
    public static boolean wrapperNotInDomain() {
        String pkg = QueryWrapper.class.getPackageName();
        return !pkg.contains("domain");
    }

    /**
     * WRAPPER-007: AST nodes are immutable.
     */
    public static boolean astNodesImmutable() {
        var field = QueryFields.of("status", String.class);
        var inNode = new InNode<>(field, List.of("A", "B"), false);
        try {
            inNode.values().add("C");
            return false;
        } catch (UnsupportedOperationException e) {
            return true;
        }
    }

    /**
     * WRAPPER-009: Empty IN must not generate illegal SQL.
     */
    public static boolean emptyInSafe() {
        var field = QueryFields.of("status", String.class);
        var wrapper = Wrappers.<Object>query().in(field, List.of());
        return wrapper.nodes().size() == 1;
    }

    /**
     * §20: All chain methods exist and return QueryWrapper.
     */
    public static boolean allChainMethodsPresent() {
        QueryField<String> f = QueryFields.of("f", String.class);
        QueryWrapper<Object> w = Wrappers.query();
        w = w.eq(f, "v");
        w = w.ne(f, "v");
        w = w.gt(f, "v");
        w = w.ge(f, "v");
        w = w.lt(f, "v");
        w = w.le(f, "v");
        w = w.isNull(f);
        w = w.isNotNull(f);
        w = w.in(f, List.of("a"));
        w = w.notIn(f, List.of("a"));
        w = w.between(f, "a", "b");
        w = w.like(f, "v");
        w = w.and(c -> {});
        w = w.or(c -> {});
        w = w.orderByAsc(f);
        w = w.orderByDesc(f);
        w = w.select(f);
        w = w.page(0, 10);
        return true;
    }

    /**
     * §62: AST nodes form sealed hierarchy.
     */
    public static boolean sealedAstHierarchy() {
        return QueryNode.class.isSealed()
                && ConditionNode.class.isSealed()
                && QueryNode.class.isAssignableFrom(ConditionNode.class)
                && QueryNode.class.isAssignableFrom(OrderNode.class)
                && QueryNode.class.isAssignableFrom(SelectNode.class)
                && QueryNode.class.isAssignableFrom(PaginationNode.class);
    }
}
