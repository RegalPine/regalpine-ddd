package io.github.regalpine.ddd.conformance;

import io.github.regalpine.ddd.transaction.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Conformance test suite for {@link TransactionAdapter} implementations.
 * <p>
 * Adapter implementations should extend this class and provide their adapter instance.
 * The tests verify correct begin/commit/rollback lifecycle behavior.
 *
 * @author RegalPine
 */
public abstract class TransactionAdapterConformance {

    /**
     * Subclasses must provide the adapter under test.
     */
    protected abstract TransactionAdapter adapter();

    /**
     * Verifies that a new adapter is not active.
     */
    @Test
    public void verifyInitiallyInactive() {
        assertFalse(adapter().isActive(), "Adapter should be initially inactive");
    }

    /**
     * Verifies that begin activates the adapter.
     */
    @Test
    public void verifyBeginActivates() {
        adapter().begin(TransactionDefinition.DEFAULT);
        assertTrue(adapter().isActive(), "Adapter should be active after begin");
        adapter().commit();
    }

    /**
     * Verifies that commit deactivates the adapter.
     */
    @Test
    public void verifyCommitDeactivates() {
        adapter().begin(TransactionDefinition.DEFAULT);
        adapter().commit();
        assertFalse(adapter().isActive(), "Adapter should be inactive after commit");
    }

    /**
     * Verifies that rollback deactivates the adapter.
     */
    @Test
    public void verifyRollbackDeactivates() {
        adapter().begin(TransactionDefinition.DEFAULT);
        adapter().rollback();
        assertFalse(adapter().isActive(), "Adapter should be inactive after rollback");
    }
}
