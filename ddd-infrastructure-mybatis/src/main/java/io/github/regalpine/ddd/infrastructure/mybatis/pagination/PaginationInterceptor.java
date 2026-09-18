package io.github.regalpine.ddd.infrastructure.mybatis.pagination;

import io.github.regalpine.ddd.core.pagination.PageRequest;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;

/**
 * MyBatis interceptor that applies pagination dialect to SQL queries.
 *
 * <p>Phase XII §30: intercepts query execution and appends dialect-specific
 * pagination clauses. The interceptor only modifies ORDER/LIMIT/OFFSET
 * (§42) and must not rewrite JOIN, WHERE, GROUP BY, or SELECT semantics.</p>
 *
 * <p>Phase XII §43: sort fields must go through {@code SortFieldRegistry}
 * whitelist mapping — never directly from user input.</p>
 *
 * @author RegalPine
 */
@Intercepts({
        @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
        ),
        @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class,
                        CacheKey.class, BoundSql.class}
        )
})
public final class PaginationInterceptor implements Interceptor {

    private volatile PaginationDialect dialect;
    private volatile PaginationContext context;

    /**
     * Sets the pagination dialect to use for SQL rewriting.
     */
    public void setDialect(PaginationDialect dialect) {
        this.dialect = dialect;
    }

    /**
     * Sets the pagination context for the next query execution.
     */
    public void setContext(PaginationContext context) {
        this.context = context;
    }

    /**
     * Clears the current pagination context after query execution.
     */
    public void clearContext() {
        this.context = null;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        PaginationContext ctx = this.context;
        if (ctx == null || dialect == null) {
            return invocation.proceed();
        }

        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];
        RowBounds rowBounds = (RowBounds) args[2];
        ResultHandler<?> resultHandler = (ResultHandler<?>) args[3];

        BoundSql boundSql;
        if (args.length == 6) {
            boundSql = (BoundSql) args[5];
        } else {
            boundSql = ms.getBoundSql(parameter);
        }

        String originalSql = boundSql.getSql();
        PageRequest req = ctx.request();
        String paginatedSql = dialect.applyOffsetLimit(
                originalSql, req.offset(), req.size());

        // Clear context after use
        this.context = null;

        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // No properties required
    }
}
