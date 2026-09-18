package io.github.regalpine.ddd.infrastructure.mybatis.query;

import io.github.regalpine.ddd.core.pagination.PageResult;
import io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper.QueryWrapper;

/**
 * Query-side repository for paginated queries using {@link QueryWrapper}.
 *
 * <p>Phase XIII §38: this is the CQRS Read Side repository. It accepts
 * a {@code QueryWrapper} (Query Infrastructure) and returns a {@link PageResult}.
 * Domain repositories (§57) must NOT accept {@code QueryWrapper}.</p>
 *
 * @author RegalPine
 */
public interface MyBatisPageQueryRepository {

    /**
     * Executes a paginated query defined by the wrapper.
     *
     * @param wrapper    the query wrapper defining conditions, ordering, and pagination
     * @param resultType the expected result element type
     * @param <T>        the result type
     * @return a page of results
     */
    <T> PageResult<T> query(QueryWrapper<?> wrapper, Class<T> resultType);
}
