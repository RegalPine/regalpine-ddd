package io.github.regalpine.ddd.infrastructure.mybatis.query;

import io.github.regalpine.ddd.core.pagination.PageRequest;
import io.github.regalpine.ddd.core.pagination.PageResult;
import io.github.regalpine.ddd.infrastructure.mybatis.pagination.PaginationDialect;
import io.github.regalpine.ddd.infrastructure.mybatis.session.MyBatisSessionAdapter;

import java.util.List;
import java.util.Objects;

/**
 * Executes paginated queries using MyBatis and the configured pagination dialect.
 *
 * <p>Phase XII §5: the query executor is the central component for
 * read-side pagination. It applies dialect-specific SQL rewriting
 * via the {@link PaginationDialect} and returns {@link PageResult}.</p>
 *
 * @author RegalPine
 */
public class MyBatisQueryExecutor {

    private final MyBatisSessionAdapter sessionAdapter;
    private final PaginationDialect dialect;

    public MyBatisQueryExecutor(
            MyBatisSessionAdapter sessionAdapter,
            PaginationDialect dialect) {
        this.sessionAdapter = Objects.requireNonNull(sessionAdapter, "sessionAdapter must not be null");
        this.dialect = Objects.requireNonNull(dialect, "dialect must not be null");
    }

    /**
     * Applies pagination to the given SQL using the configured dialect.
     *
     * @param sql         the base SQL query
     * @param pageRequest the pagination request
     * @return the paginated SQL
     */
    public String applyPagination(String sql, PageRequest pageRequest) {
        return dialect.applyOffsetLimit(
                sql,
                pageRequest.offset(),
                pageRequest.size()
        );
    }

    /**
     * Returns the pagination dialect in use.
     */
    public PaginationDialect dialect() {
        return dialect;
    }

    /**
     * Returns the session adapter.
     */
    protected MyBatisSessionAdapter sessionAdapter() {
        return sessionAdapter;
    }
}
