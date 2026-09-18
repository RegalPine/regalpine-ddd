package io.github.regalpine.ddd.cqrs.bus;

import io.github.regalpine.ddd.application.query.Query;
import io.github.regalpine.ddd.application.query.QueryHandler;
import io.github.regalpine.ddd.cqrs.middleware.InvocationContext;
import io.github.regalpine.ddd.cqrs.middleware.Middleware;
import io.github.regalpine.ddd.cqrs.middleware.MiddlewareChain;
import io.github.regalpine.ddd.cqrs.registry.QueryHandlerRegistry;

import java.util.List;
import java.util.Objects;

/**
 * Default {@link QueryBus} implementation with middleware pipeline support.
 *
 * @author RegalPine
 */
public final class DefaultQueryBus implements QueryBus {

    private final QueryHandlerRegistry registry;
    private final List<Middleware<?>> middlewares;

    public DefaultQueryBus(QueryHandlerRegistry registry) {
        this(registry, List.of());
    }

    public DefaultQueryBus(QueryHandlerRegistry registry, List<Middleware<?>> middlewares) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.middlewares = List.copyOf(middlewares);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <R> R dispatch(Query<R> query) {
        Objects.requireNonNull(query, "query must not be null");
        Class<? extends Query> queryType = (Class<? extends Query>) query.getClass();
        QueryHandler handler = registry.find(queryType);

        InvocationContext<R> context = new InvocationContext<>(query, query.getClass().getCanonicalName());
        MiddlewareChain<R> chain = new MiddlewareChain<>((List) middlewares);

        return chain.execute(context, () -> (R) handler.handle(query));
    }
}
