package io.github.regalpine.ddd.infrastructure.mybatis.repository;

import io.github.regalpine.ddd.core.aggregate.AggregateRoot;
import io.github.regalpine.ddd.core.identifier.Identifier;
import io.github.regalpine.ddd.core.version.Version;

import java.util.Optional;

/**
 * 聚合 SQL 执行策略。实现负责映射、租户约束和关联写入，事务由外层管理。
 * 主表写入必须先检查影响行数，只有主表成功才允许写入关联数据。
 * 方法返回主表影响行数；数据库约束异常应原样传播，不可降级为新增。
 */
public interface AggregatePersistenceOperations<A extends AggregateRoot<I>, I extends Identifier> {
    Optional<A> findById(I id);

    int insert(A aggregate, Version persistedVersion);

    int update(A aggregate, Version expectedVersion, Version persistedVersion);

    /** 不支持删除的适配器必须明确抛出异常，不能静默成功。 */
    int delete(A aggregate, Version expectedVersion);
}
