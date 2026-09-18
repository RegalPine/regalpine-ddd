package io.github.regalpine.ddd.infrastructure.mybatis.query;

import io.github.regalpine.ddd.infrastructure.mybatis.query.ast.ConditionNode;

import java.util.List;

/**
 * Provides system-level query predicates that are prepended to user queries.
 *
 * <p>Phase XIII §53: system predicates are controlled by the framework/application
 * and cannot be overridden by user-level {@code QueryWrapper} calls. They enforce:</p>
 * <ul>
 *   <li>Tenant isolation (§52): {@code WHERE tenant_id = ?}</li>
 *   <li>Soft delete (§54): {@code WHERE deleted = false}</li>
 *   <li>Data scope (§55): department/role-based filtering</li>
 * </ul>
 *
 * <p>Final SQL structure:</p>
 * <pre>
 * WHERE system_predicate AND user_predicate
 * </pre>
 *
 * @author RegalPine
 */
public interface QueryPredicateProvider {

    /**
     * Returns the system-level condition nodes.
     *
     * @return an immutable list of system predicates
     */
    List<ConditionNode> predicates();
}
