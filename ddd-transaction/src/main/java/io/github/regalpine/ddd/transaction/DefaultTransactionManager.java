package io.github.regalpine.ddd.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 技术无关的事务传播、同步与物理资源所有权协调器。 */
public class DefaultTransactionManager implements TransactionManager {
    private final TransactionAdapter adapter;
    private final ThreadLocal<Scope> current = new ThreadLocal<>();

    public DefaultTransactionManager(TransactionAdapter adapter) {
        this.adapter = Objects.requireNonNull(adapter, "adapter");
    }

    @Override
    public <T> T execute(TransactionCallback<T> callback) {
        return execute(TransactionDefinition.DEFAULT, callback);
    }

    @Override
    public <T> T execute(TransactionDefinition definition, TransactionCallback<T> callback) {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(callback, "callback");
        Scope existing = current.get();
        if (existing == null && adapter.isActive()) {
            throw new IllegalStateException("检测到不由本协调器拥有的物理事务");
        }
        return switch (definition.propagation()) {
            case REQUIRED -> existing == null ? begin(definition, callback) : join(existing, callback);
            case MANDATORY -> {
                if (existing == null) throw new IllegalStateException("MANDATORY 要求活动事务");
                yield join(existing, callback);
            }
            case NEVER -> {
                if (existing != null) throw new IllegalStateException("NEVER 不允许活动事务");
                yield callback.execute();
            }
            case SUPPORTS -> existing == null ? callback.execute() : join(existing, callback);
            case REQUIRES_NEW -> suspended(existing, () -> begin(definition, callback));
            case NOT_SUPPORTED -> suspended(existing, callback);
        };
    }

    private <T> T suspended(Scope existing, TransactionCallback<T> callback) {
        if (existing == null) return callback.execute();
        Object resource = adapter.suspend();
        current.remove();
        Throwable failure = null;
        try {
            return callback.execute();
        } catch (RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            try {
                adapter.resume(resource);
            } catch (RuntimeException | Error e) {
                existing.rollbackOnly = true;
                if (failure != null) failure.addSuppressed(e);
                else throw e;
            } finally {
                current.set(existing);
            }
        }
    }

    private <T> T join(Scope scope, TransactionCallback<T> callback) {
        if (scope.completing) throw new IllegalStateException("事务正在完成，不能加入新的工作");
        try {
            scope.checkTimeout();
            return callback.execute();
        } catch (RuntimeException | Error e) {
            scope.rollbackOnly = true;
            throw e;
        }
    }

    private <T> T begin(TransactionDefinition definition, TransactionCallback<T> callback) {
        Scope scope = new Scope(definition);
        adapter.begin(definition);
        current.set(scope);
        boolean committed = false;
        Throwable failure = null;
        try {
            T result = callback.execute();
            scope.completing = true;
            scope.checkTimeout();
            if (scope.rollbackOnly) throw new IllegalStateException("事务已标记 rollback-only");
            for (TransactionSynchronization synchronization : List.copyOf(scope.synchronizations)) {
                synchronization.beforeCommit();
            }
            scope.checkTimeout();
            if (scope.rollbackOnly) throw new IllegalStateException("事务已标记 rollback-only");
            adapter.commit();
            committed = true;
            // 提交后回调不能误加入已完成的事务。
            current.remove();
            adapter.close();
            RuntimeException notificationFailure = null;
            for (TransactionSynchronization synchronization : scope.synchronizations) {
                try {
                    synchronization.afterCommit();
                } catch (RuntimeException e) {
                    if (notificationFailure == null) notificationFailure = e;
                    else notificationFailure.addSuppressed(e);
                }
            }
            if (notificationFailure != null) throw notificationFailure;
            return result;
        } catch (RuntimeException | Error e) {
            failure = committed ? new TransactionCommittedException(e) : e;
            if (!committed) {
                try { adapter.rollback(); } catch (Throwable cleanup) { failure.addSuppressed(cleanup); }
                for (TransactionSynchronization synchronization : scope.synchronizations) {
                    try { synchronization.afterRollback(); } catch (Throwable cleanup) { failure.addSuppressed(cleanup); }
                }
            }
            if (failure instanceof Error error) throw error;
            throw (RuntimeException) failure;
        } finally {
            current.remove();
            try {
                adapter.close();
            } catch (RuntimeException | Error cleanup) {
                if (failure != null) failure.addSuppressed(cleanup);
                else if (committed) throw new TransactionCommittedException(cleanup);
                else throw cleanup;
            }
        }
    }

    @Override
    public void execute(TransactionRunnable runnable) { execute(TransactionDefinition.DEFAULT, runnable); }

    @Override
    public void execute(TransactionDefinition definition, TransactionRunnable runnable) {
        Objects.requireNonNull(runnable, "runnable");
        execute(definition, () -> { runnable.run(); return null; });
    }

    @Override
    public boolean isActive() { return current.get() != null; }

    @Override
    public void setRollbackOnly() { requireScope().rollbackOnly = true; }

    @Override
    public void registerSynchronization(TransactionSynchronization synchronization) {
        Scope scope = requireScope();
        if (scope.completing) throw new IllegalStateException("事务回调列表已冻结");
        scope.synchronizations.add(Objects.requireNonNull(synchronization, "synchronization"));
    }

    private Scope requireScope() {
        return Objects.requireNonNull(current.get(), "没有活动事务");
    }

    private static final class Scope {
        final long started = System.nanoTime();
        final long timeout;
        final List<TransactionSynchronization> synchronizations = new ArrayList<>();
        boolean rollbackOnly;
        boolean completing;
        Scope(TransactionDefinition definition) {
            timeout = definition.timeout().isZero() ? 0 : definition.timeout().toNanos();
        }
        void checkTimeout() {
            if (timeout > 0 && System.nanoTime() - started >= timeout) {
                rollbackOnly = true;
                throw new IllegalStateException("事务超时");
            }
        }
    }
}
