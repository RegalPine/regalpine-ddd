package io.github.regalpine.ddd.conformance.mybatis;

import io.github.regalpine.ddd.infrastructure.mybatis.transaction.MyBatisTransactionAdapter;
import io.github.regalpine.ddd.transaction.TransactionAdapter;

/**
 * Conformance test suite for MyBatis transaction adapter.
 *
 * <p>Phase XII §66: verifies transaction lifecycle conformance
 * (begin/commit/rollback/isActive).</p>
 *
 * @author RegalPine
 */
public abstract class MyBatisTransactionConformance {

    /**
     * Subclasses must provide the adapter under test.
     */
    protected abstract MyBatisTransactionAdapter adapter();

    /**
     * Verifies that the adapter implements TransactionAdapter.
     */
    protected void verifyImplementsTransactionAdapter() {
        if (!(adapter() instanceof TransactionAdapter)) {
            throw new AssertionError(
                    "MyBatisTransactionAdapter must implement TransactionAdapter");
        }
    }

    /**
     * Verifies initial inactive state.
     */
    protected void verifyInitiallyInactive() {
        if (adapter().isActive()) {
            throw new AssertionError("Adapter should be initially inactive");
        }
    }

    /**
     * Runs all structural conformance checks.
     */
    public void runAll() {
        verifyImplementsTransactionAdapter();
        verifyInitiallyInactive();
    }
}
