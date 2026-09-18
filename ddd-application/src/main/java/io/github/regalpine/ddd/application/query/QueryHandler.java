package io.github.regalpine.ddd.application.query;

/**
 * Handles a specific type of query.
 *
 * <p>A QueryHandler retrieves data from read models or query repositories.
 * It does not load aggregates or invoke domain behavior.</p>
 *
 * @param <Q> the query type
 * @param <R> the result type
 */
public interface QueryHandler<Q extends Query<R>, R> {

    /**
     * Handles the given query.
     *
     * @param query the query to handle
     * @return the query result
     */
    R handle(Q query);
}
