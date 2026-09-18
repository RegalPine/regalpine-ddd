package io.github.regalpine.ddd.cqrs.middleware;

/**
 * Middleware that intercepts command or query dispatch.
 * <p>
 * Middlewares are composed into a chain via {@link MiddlewareChain}.
 * Each middleware can perform pre/post processing around the {@link InvocationChain#proceed()} call.
 *
 * @param <R> the result type
 * @author RegalPine
 */
@FunctionalInterface
public interface Middleware<R> {

    /**
     * Intercepts the dispatch pipeline.
     *
     * @param context the invocation context containing the message
     * @param chain   the next step in the chain
     * @return the result
     */
    R execute(InvocationContext<R> context, InvocationChain<R> chain);
}
