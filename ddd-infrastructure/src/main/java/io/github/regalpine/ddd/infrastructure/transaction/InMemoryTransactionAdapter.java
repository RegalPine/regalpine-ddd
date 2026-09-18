package io.github.regalpine.ddd.infrastructure.transaction;

import io.github.regalpine.ddd.transaction.*;
import java.util.Objects;

/** 仅模拟事务边界，不提供数据回滚能力。 */
public final class InMemoryTransactionAdapter implements TransactionAdapter {
    private final ThreadLocal<Boolean> active = new ThreadLocal<>();
    private volatile boolean failOnCommit;

    @Override
    public void begin(TransactionDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        if (isActive()) throw new IllegalStateException("Transaction already active");
        active.set(true);
    }
    @Override
    public void commit() {
        if (!isActive()) throw new IllegalStateException("No active transaction to commit");
        if (failOnCommit) {
            failOnCommit = false;
            throw new IllegalStateException("Simulated commit failure");
        }
        active.remove();
    }
    @Override public void rollback() { active.remove(); }
    @Override public void close() { active.remove(); }
    @Override public boolean isActive() { return Boolean.TRUE.equals(active.get()); }
    @Override public Object suspend() {
        if (!isActive()) throw new IllegalStateException("没有活动事务");
        active.remove();
        return Boolean.TRUE;
    }
    @Override public void resume(Object resource) {
        if (isActive() || !Boolean.TRUE.equals(resource)) throw new IllegalStateException("无效恢复状态");
        active.set(true);
    }
    public void setFailOnCommit(boolean fail) { failOnCommit = fail; }
}
