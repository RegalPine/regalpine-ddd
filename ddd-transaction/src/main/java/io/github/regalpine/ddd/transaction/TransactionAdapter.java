package io.github.regalpine.ddd.transaction;

/**
 * Port for adapting to specific transaction infrastructure (JDBC, JPA, etc.).
 * <p>
 * Each adapter translates framework-level transaction operations into
 * infrastructure-specific implementations.
 *
 * @author RegalPine
 */
public interface TransactionAdapter {

    /**
     * Begins a new transaction with the given definition.
     *
     * @param definition the transaction definition
     */
    void begin(TransactionDefinition definition);

    /**
     * Commits the current transaction.
     */
    void commit();

    /**
     * Rolls back the current transaction.
     */
    void rollback();

    /**
     * Returns whether a transaction is currently active.
     */
    boolean isActive();

    /** 挂起当前物理资源；返回的令牌仅供同一适配器恢复。 */
    default Object suspend() {
        throw new UnsupportedOperationException("适配器不支持事务挂起");
    }

    default void resume(Object resource) {
        throw new UnsupportedOperationException("适配器不支持事务恢复");
    }

    /** 释放当前范围拥有的资源，不执行提交。 */
    default void close() {
    }
}
