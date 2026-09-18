package io.github.regalpine.ddd.infrastructure.mybatis.repository;

import io.github.regalpine.ddd.core.version.Version;

/**
 * 全部 SQL 写入成功后的版本同步回调，不代表事务已经提交。
 * 事务适配器负责注册回滚恢复逻辑；实现不得替换聚合实例或清除领域事件。
 */
@FunctionalInterface
public interface PersistenceVersionSynchronizer<A> {
    void synchronize(A aggregate, Version expectedVersion, Version persistedVersion);
}
