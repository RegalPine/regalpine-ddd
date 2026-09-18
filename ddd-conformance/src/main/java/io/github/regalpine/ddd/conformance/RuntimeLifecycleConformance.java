package io.github.regalpine.ddd.conformance;

import io.github.regalpine.ddd.runtime.DddRuntime;
import io.github.regalpine.ddd.runtime.RuntimeState;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Conformance test suite for {@link DddRuntime} lifecycle implementations.
 *
 * <p>Per Phase VIII §139, runtime implementations must pass the
 * Runtime Conformance Kit verifying lifecycle, component discovery,
 * dependency graph, and shutdown behavior.</p>
 *
 * <p>Adapter implementations should extend this class and provide
 * their runtime instance via the abstract method.</p>
 *
 * @author RegalPine
 */
public abstract class RuntimeLifecycleConformance {

    /**
     * Subclasses must provide the runtime under test.
     */
    protected abstract DddRuntime runtime();

    /**
     * Subclasses must provide a fresh (CREATED state) runtime instance.
     */
    protected abstract DddRuntime freshRuntime();

    /**
     * Verifies the runtime starts in CREATED state.
     */
    @Test
    public void verifyInitialState() {
        DddRuntime runtime = freshRuntime();
        assertEquals(RuntimeState.CREATED, runtime.state());
    }

    /**
     * Verifies the full lifecycle: CREATED -> RUNNING -> STOPPED.
     */
    @Test
    public void verifyLifecycleTransitions() {
        DddRuntime runtime = freshRuntime();
        assertEquals(RuntimeState.CREATED, runtime.state());

        runtime.start();
        assertEquals(RuntimeState.RUNNING, runtime.state());

        runtime.shutdown();
        assertEquals(RuntimeState.STOPPED, runtime.state());
    }

    /**
     * Verifies AutoCloseable support (close delegates to shutdown).
     */
    @Test
    public void verifyAutoCloseable() {
        DddRuntime runtime = freshRuntime();
        runtime.start();
        assertEquals(RuntimeState.RUNNING, runtime.state());

        runtime.close();
        assertEquals(RuntimeState.STOPPED, runtime.state());
    }
}
