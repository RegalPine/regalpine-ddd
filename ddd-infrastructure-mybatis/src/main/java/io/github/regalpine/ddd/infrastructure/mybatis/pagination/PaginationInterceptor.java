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
    private final ThreadLocal<PaginationContext> context = new ThreadLocal<>();

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
        this.context.set(java.util.Objects.requireNonNull(context, "context"));
    }

    /**
     * Clears the current pagination context after query execution.
     */
    public void clearContext() {
        this.context.remove();
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        PaginationContext ctx = context.get();
        if (ctx == null) {
            return invocation.proceed();
        }
        // 在进入执行器前消费上下文，嵌套查询不得继承本次分页。
        context.remove();
        try {
            PaginationDialect currentDialect = java.util.Objects.requireNonNull(dialect,
                    "分页查询必须配置 dialect");
            Object[] args = invocation.getArgs();
            MappedStatement ms = (MappedStatement) args[0];
            Object parameter = args[1];
            BoundSql original = args.length == 6 ? (BoundSql) args[5] : ms.getBoundSql(parameter);
            PageRequest request = ctx.request();
            BoundSql paged = new BoundSql(ms.getConfiguration(),
                    currentDialect.applyOffsetLimit(original.getSql(), request.offset(), request.size()),
                    original.getParameterMappings(), original.getParameterObject());
            original.getAdditionalParameters().forEach(paged::setAdditionalParameter);
            Executor executor = (Executor) invocation.getTarget();
            CacheKey key = executor.createCacheKey(ms, parameter, RowBounds.DEFAULT, paged);
            if (args.length == 6) {
                args[2] = RowBounds.DEFAULT;
                args[4] = key;
                args[5] = paged;
                return invocation.proceed();
            }
            // 六参数入口显式传递 BoundSql，无需修改共享 MappedStatement。
            return executor.query(ms, parameter, RowBounds.DEFAULT, (ResultHandler<?>) args[3], key, paged);
        } finally {
            context.remove();
        }
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
