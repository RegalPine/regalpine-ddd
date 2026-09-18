package io.github.regalpine.ddd.infrastructure.mybatis.query;

import io.github.regalpine.ddd.core.pagination.PageRequest;
import io.github.regalpine.ddd.core.pagination.PageResult;

import java.util.List;

/**
 * Read-side repository for MyBatis query operations.
 *
 * <p>Phase XII §5/§53: provides paginated access to read models
 * without hydrating full aggregates. Query results are returned
 * directly as lightweight view objects.</p>
 *
 * @param <C> the condition/criteria type
 * @param <R> the read model type
 * @author RegalPine
 */
public interface MyBatisReadRepository<C, R> {

    /**
     * Queries for a page of read models matching the given condition.
     *
     * @param condition   the query condition
     * @param pageRequest the pagination request
     * @return a page of read model results
     */
    PageResult<R> query(C condition, PageRequest pageRequest);
}
