package io.github.regalpine.ddd.infrastructure.mybatis.pagination;

import io.github.regalpine.ddd.core.pagination.PageRequest;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.session.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaginationInterceptorTest {
    @Test
    void shouldRewriteBothSignaturesWithoutMutatingOriginal() throws Throwable {
        for (boolean sixArguments : List.of(false, true)) {
            var interceptor = new PaginationInterceptor();
            PaginationDialect dialect = mock(PaginationDialect.class);
            when(dialect.applyOffsetLimit(anyString(), eq(10L), eq(5L))).thenReturn("SELECT ? LIMIT 5 OFFSET 10");
            interceptor.setDialect(dialect);
            var configuration = new Configuration();
            var mapping = new ParameterMapping.Builder(configuration, "item", Integer.class).build();
            var original = new BoundSql(configuration, "SELECT ?", List.of(mapping), Map.of());
            original.setAdditionalParameter("item", 42);
            var statement = new MappedStatement.Builder(configuration, "query", p -> original, SqlCommandType.SELECT).build();
            Executor executor = mock(Executor.class);
            CacheKey key = new CacheKey();
            when(executor.createCacheKey(eq(statement), any(), eq(RowBounds.DEFAULT), any())).thenReturn(key);
            when(executor.query(eq(statement), any(), eq(RowBounds.DEFAULT), isNull(), eq(key), any())).thenAnswer(call -> {
                BoundSql sql = call.getArgument(5);
                assertThat(sql.getSql()).isEqualTo("SELECT ? LIMIT 5 OFFSET 10");
                assertThat(sql.getParameterMappings()).containsExactly(mapping);
                assertThat(sql.getAdditionalParameter("item")).isEqualTo(42);
                return List.of(42);
            });
            interceptor.setContext(new PaginationContext(new PageRequest(2, 5, null, null)));
            Class<?>[] types = sixArguments
                    ? new Class<?>[]{MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}
                    : new Class<?>[]{MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class};
            Object[] args = sixArguments
                    ? new Object[]{statement, Map.of(), new RowBounds(3, 8), null, new CacheKey(), original}
                    : new Object[]{statement, Map.of(), new RowBounds(3, 8), null};
            assertThat(interceptor.intercept(new Invocation(executor, Executor.class.getMethod("query", types), args)))
                    .isEqualTo(List.of(42));
            assertThat(original.getSql()).isEqualTo("SELECT ?");
            assertThat(statement.getBoundSql(Map.of())).isSameAs(original);
        }
    }

    @Test
    void shouldIsolateThreadsAndClearAfterFailure() throws Exception {
        var interceptor = new PaginationInterceptor();
        var executor = mock(Executor.class);
        var method = Executor.class.getMethod("query", MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class);
        var invocation = new Invocation(executor, method, new Object[]{null, null, RowBounds.DEFAULT, null});
        interceptor.setContext(new PaginationContext(new PageRequest(0, 5, null, null)));
        var pool = Executors.newSingleThreadExecutor();
        try {
            pool.submit(() -> {
                assertThatCode(() -> interceptor.intercept(invocation)).doesNotThrowAnyException();
            }).get();
            assertThatThrownBy(() -> interceptor.intercept(invocation)).isInstanceOf(NullPointerException.class);
            assertThatCode(() -> interceptor.intercept(invocation)).doesNotThrowAnyException();
        } finally {
            pool.shutdownNow();
        }
    }
}
