package io.github.regalpine.ddd.infrastructure.transaction;

import io.github.regalpine.ddd.transaction.DefaultTransactionManager;
import io.github.regalpine.ddd.transaction.TransactionAdapter;

/** 开发测试用入口；生产环境应显式提供持久化事务适配器。 */
public final class InMemoryTransactionManager extends DefaultTransactionManager {
    public InMemoryTransactionManager() {
        super(new InMemoryTransactionAdapter());
    }

    public InMemoryTransactionManager(TransactionAdapter adapter) {
        super(adapter);
    }
}
