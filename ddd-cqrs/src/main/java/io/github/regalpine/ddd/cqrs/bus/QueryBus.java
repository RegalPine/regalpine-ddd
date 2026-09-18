package io.github.regalpine.ddd.cqrs.bus;

import io.github.regalpine.ddd.application.query.Query;

/**
 * Query bus that dispatches queries to their registered handlers.
 * <p>
 * Each query type must have exactly one handler. Duplicate handlers cause startup failure.
 *
 * @author RegalPine
 */
public interface QueryBus {

    /**
     * Dispatches a query to its registered handler.
     *
     * @param query the query to dispatch
     * @param <R>   the result type
     * @return the handler result
     * @throws HandlerNotFoundException  if no handler is registered for the query type
     * @throws DuplicateHandlerException if multiple handlers are registered for the same query type
     */
    <R> R dispatch(Query<R> query);
}
