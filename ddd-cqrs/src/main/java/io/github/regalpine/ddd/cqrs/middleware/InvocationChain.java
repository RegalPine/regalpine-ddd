package io.github.regalpine.ddd.cqrs.middleware;

/**
 * Represents the next step in a middleware chain.
 *
 * @param <R> the result type
 * @author RegalPine
 */
@FunctionalInterface
public interface InvocationChain<R> {

    /**
     * Proceeds to the next middleware or the final handler.
     *
     * @return the result of the downstream invocation
     */
    R proceed();
}
