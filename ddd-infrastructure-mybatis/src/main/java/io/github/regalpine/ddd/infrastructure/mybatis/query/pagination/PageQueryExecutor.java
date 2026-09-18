package io.github.regalpine.ddd.infrastructure.mybatis.query.pagination;

import io.github.regalpine.ddd.core.pagination.PageResult;
import io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper.QueryWrapper;

/**
 * Executes paginated queries using a {@link QueryWrapper}.
 *
 * <p>Phase XIII §37: the executor takes a query wrapper and result type,
 * translates the wrapper to SQL, applies pagination via the dialect,
 * and returns a {@link PageResult}.</p>
 *
 * @author RegalPine
 */
public interface PageQueryExecutor {

    /**
     * Executes the query defined by the wrapper and returns a page of results.
     *
     * @param wrapper    the query wrapper defining conditions, ordering, and pagination
     * @param resultType the expected result element type
     * @param <T>        the result type
     * @return a page of results
     */
    <T> PageResult<T> execute(QueryWrapper<?> wrapper, Class<T> resultType);
}
