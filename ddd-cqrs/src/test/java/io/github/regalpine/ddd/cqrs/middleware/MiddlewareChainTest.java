package io.github.regalpine.ddd.cqrs.middleware;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class MiddlewareChainTest {

    @Test
    void emptyChainShouldCallTerminalHandler() {
        var chain = new MiddlewareChain<String>();
        InvocationContext<String> ctx = new InvocationContext<>("msg", "testType");

        String result = chain.execute(ctx, () -> "terminal");

        assertThat(result).isEqualTo("terminal");
    }

    @Test
    void singleMiddlewareShouldWrapHandler() {
        var chain = new MiddlewareChain<String>();
        chain.add((ctx, next) -> "before-" + next.proceed() + "-after");
        InvocationContext<String> ctx = new InvocationContext<>("msg", "testType");

        String result = chain.execute(ctx, () -> "handler");

        assertThat(result).isEqualTo("before-handler-after");
    }

    @Test
    void multipleMiddlewaresShouldExecuteInOrder() {
        var chain = new MiddlewareChain<String>();
        List<String> order = new ArrayList<>();

        chain.add((ctx, next) -> {
            order.add("mw1-before");
            String r = next.proceed();
            order.add("mw1-after");
            return r;
        });
        chain.add((ctx, next) -> {
            order.add("mw2-before");
            String r = next.proceed();
            order.add("mw2-after");
            return r;
        });

        InvocationContext<String> ctx = new InvocationContext<>("msg", "testType");
        String result = chain.execute(ctx, () -> {
            order.add("handler");
            return "done";
        });

        assertThat(result).isEqualTo("done");
        assertThat(order).containsExactly("mw2-before", "mw1-before", "handler", "mw1-after", "mw2-after");
    }

    @Test
    void middlewaresShouldReturnUnmodifiableList() {
        var chain = new MiddlewareChain<String>();
        chain.add((ctx, next) -> next.proceed());

        assertThat(chain.middlewares()).hasSize(1);
        assertThatThrownBy(() -> chain.middlewares().add((ctx, next) -> next.proceed()))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
