package io.github.regalpine.ddd.cqrs.registry;

import io.github.regalpine.ddd.application.query.Query;
import io.github.regalpine.ddd.application.query.QueryHandler;

/**
 * Registry for query handlers.
 * <p>
 * Enforces one handler per query type. Registering a duplicate throws {@link
 * io.github.regalpine.ddd.cqrs.bus.DuplicateHandlerException}.
 *
 * @author RegalPine
 */
public interface QueryHandlerRegistry {

    /**
     * Registers a handler for the query type extracted from the handler's generic parameter.
     *
     * @param handler the query handler
     * @param <Q>     the query type
     * @param <R>     the result type
     * @throws io.github.regalpine.ddd.cqrs.bus.DuplicateHandlerException if a handler is already registered
     */
    <Q extends Query<R>, R> void register(QueryHandler<Q, R> handler);

    /**
     * Finds the handler for the given query type.
     *
     * @param queryType the query class
     * @param <Q>       the query type
     * @param <R>       the result type
     * @return the registered handler
     * @throws io.github.regalpine.ddd.cqrs.bus.HandlerNotFoundException if no handler is found
     */
    <Q extends Query<R>, R> QueryHandler<Q, R> find(Class<Q> queryType);
}
