package io.github.regalpine.ddd.infrastructure.transaction;

import io.github.regalpine.ddd.transaction.*;

import java.util.Objects;

/**
 * In-memory implementation of {@link TransactionManager} for testing and development.
 * <p>
 * Uses {@link InMemoryTransactionAdapter} to simulate transaction boundaries.
 *
 * @author RegalPine
 */
public final class InMemoryTransactionManager implements TransactionManager {

    private final TransactionAdapter adapter;

    public InMemoryTransactionManager() {
        this(new InMemoryTransactionAdapter());
    }

    public InMemoryTransactionManager(TransactionAdapter adapter) {
        this.adapter = Objects.requireNonNull(adapter, "adapter must not be null");
    }

    @Override
    public <T> T execute(TransactionCallback<T> callback) {
        return execute(TransactionDefinition.DEFAULT, callback);
    }

    @Override
    public <T> T execute(TransactionDefinition definition, TransactionCallback<T> callback) {
        Objects.requireNonNull(callback, "callback must not be null");
        adapter.begin(definition);
        try {
            T result = callback.execute();
            adapter.commit();
            return result;
        } catch (Exception e) {
            adapter.rollback();
            throw e;
        }
    }

    @Override
    public void execute(TransactionRunnable runnable) {
        execute(TransactionDefinition.DEFAULT, runnable);
    }

    @Override
    public void execute(TransactionDefinition definition, TransactionRunnable runnable) {
        Objects.requireNonNull(runnable, "runnable must not be null");
        adapter.begin(definition);
        try {
            runnable.run();
            adapter.commit();
        } catch (Exception e) {
            adapter.rollback();
            throw e;
        }
    }
}
