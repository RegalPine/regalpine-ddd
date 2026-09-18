package io.github.regalpine.ddd.cqrs.registry;

import io.github.regalpine.ddd.application.query.Query;
import io.github.regalpine.ddd.application.query.QueryHandler;
import io.github.regalpine.ddd.cqrs.bus.DuplicateHandlerException;
import io.github.regalpine.ddd.cqrs.bus.HandlerNotFoundException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default in-memory implementation of {@link QueryHandlerRegistry}.
 *
 * @author RegalPine
 */
public final class DefaultQueryHandlerRegistry implements QueryHandlerRegistry {

    private final Map<Class<?>, QueryHandler<?, ?>> handlers = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <Q extends Query<R>, R> void register(QueryHandler<Q, R> handler) {
        Objects.requireNonNull(handler, "handler must not be null");
        Class<Q> queryType = resolveQueryType(handler);
        QueryHandler<?, ?> existing = handlers.putIfAbsent(queryType, handler);
        if (existing != null) {
            throw new DuplicateHandlerException(queryType.getCanonicalName());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <Q extends Query<R>, R> QueryHandler<Q, R> find(Class<Q> queryType) {
        Objects.requireNonNull(queryType, "queryType must not be null");
        QueryHandler<?, ?> handler = handlers.get(queryType);
        if (handler == null) {
            throw new HandlerNotFoundException(queryType.getCanonicalName());
        }
        return (QueryHandler<Q, R>) handler;
    }

    private <Q extends Query<R>, R> Class<Q> resolveQueryType(QueryHandler<Q, R> handler) {
        for (Type iface : handler.getClass().getGenericInterfaces()) {
            if (iface instanceof ParameterizedType pt
                    && pt.getRawType() == QueryHandler.class) {
                Type queryTypeArg = pt.getActualTypeArguments()[0];
                if (queryTypeArg instanceof Class<?> clazz) {
                    @SuppressWarnings("unchecked")
                    Class<Q> queryClass = (Class<Q>) clazz;
                    return queryClass;
                }
            }
        }
        throw new IllegalArgumentException(
                "Cannot resolve query type from handler: " + handler.getClass().getCanonicalName()
                        + ". Ensure the handler implements QueryHandler with concrete type parameters.");
    }
}
