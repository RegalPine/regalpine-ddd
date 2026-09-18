package io.github.regalpine.ddd.cqrs.middleware;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A composable chain of {@link Middleware} instances.
 * <p>
 * Middlewares are applied in order; the terminal action is the handler invocation.
 *
 * @param <R> the result type
 * @author RegalPine
 */
public final class MiddlewareChain<R> {

    private final List<Middleware<R>> middlewares;

    public MiddlewareChain() {
        this.middlewares = new ArrayList<>();
    }

    public MiddlewareChain(List<Middleware<R>> middlewares) {
        this.middlewares = new ArrayList<>(middlewares);
    }

    /**
     * Adds a middleware to the chain.
     */
    public MiddlewareChain<R> add(Middleware<R> middleware) {
        Objects.requireNonNull(middleware, "middleware must not be null");
        middlewares.add(middleware);
        return this;
    }

    /**
     * Returns an unmodifiable view of the middlewares.
     */
    public List<Middleware<R>> middlewares() {
        return Collections.unmodifiableList(middlewares);
    }

    /**
     * Executes the chain for the given context, terminating with the provided handler action.
     *
     * @param context the invocation context
     * @param handler the terminal handler action
     * @return the result
     */
    public R execute(InvocationContext<R> context, java.util.function.Supplier<R> handler) {
        InvocationChain<R> chain = buildChain(context, handler, middlewares.size() - 1);
        return chain.proceed();
    }

    private InvocationChain<R> buildChain(
            InvocationContext<R> context,
            java.util.function.Supplier<R> terminal,
            int index) {
        if (index < 0) {
            return terminal::get;
        }
        Middleware<R> current = middlewares.get(index);
        InvocationChain<R> next = buildChain(context, terminal, index - 1);
        return () -> current.execute(context, next);
    }
}
