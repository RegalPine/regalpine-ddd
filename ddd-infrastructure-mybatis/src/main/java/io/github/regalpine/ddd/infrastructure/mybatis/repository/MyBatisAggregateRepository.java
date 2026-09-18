package io.github.regalpine.ddd.infrastructure.mybatis.repository;

import io.github.regalpine.ddd.core.aggregate.AggregateRoot;
import io.github.regalpine.ddd.core.identifier.Identifier;
import io.github.regalpine.ddd.core.version.Version;
import io.github.regalpine.ddd.domain.repository.AggregateRepository;
import io.github.regalpine.ddd.infrastructure.mybatis.exception.MyBatisConcurrencyException;
import io.github.regalpine.ddd.infrastructure.mybatis.mapping.AggregateMapper;
import io.github.regalpine.ddd.infrastructure.mybatis.session.MyBatisSessionAdapter;

import java.util.Objects;
import java.util.Optional;

/**
 * MyBatis-based aggregate repository implementation.
 *
 * <p>Phase XII §7: implements {@link AggregateRepository} using MyBatis mappers.
 * The save operation distinguishes between INSERT (new aggregate) and UPDATE
 * (existing aggregate with optimistic concurrency check, §11/§12).</p>
 *
 * <p>The repository does NOT commit transactions (§14/§66). Transaction
 * boundaries are managed by the {@code TransactionAdapter}.</p>
 *
 * @param <A> the aggregate root type
 * @param <I> the identifier type
 * @author RegalPine
 */
public class MyBatisAggregateRepository<
        A extends AggregateRoot<I>,
        I extends Identifier>
        implements AggregateRepository<A, I> {

    private final MyBatisSessionAdapter sessionAdapter;
    private final AggregateMapper<A, ?> mapper;
    private final AggregatePersistenceOperations<A, I> operations;
    private final PersistenceVersionSynchronizer<A> versionSynchronizer;

    public MyBatisAggregateRepository(
            AggregatePersistenceOperations<A, I> operations,
            PersistenceVersionSynchronizer<A> versionSynchronizer) {
        this.operations = Objects.requireNonNull(operations, "operations must not be null");
        this.versionSynchronizer = Objects.requireNonNull(versionSynchronizer, "versionSynchronizer must not be null");
        this.sessionAdapter = null;
        this.mapper = null;
    }

    /**
     * 兼容旧子类的构造入口；未覆盖 CRUD 的调用将明确失败。
     * 新实现必须注入完整执行策略，不能仅依赖 session 与对象转换器。
     */
    @Deprecated
    public MyBatisAggregateRepository(
            MyBatisSessionAdapter sessionAdapter,
            AggregateMapper<A, ?> mapper) {
        this.sessionAdapter = Objects.requireNonNull(sessionAdapter, "sessionAdapter must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
        this.operations = null;
        this.versionSynchronizer = null;
    }

    @Override
    public Optional<A> findById(I id) {
        Objects.requireNonNull(id, "id must not be null");
        return Objects.requireNonNull(operations().findById(id), "findById must return an Optional");
    }

    @Override
    public A save(A aggregate) {
        Objects.requireNonNull(aggregate, "aggregate must not be null");
        AggregatePersistenceOperations<A, I> executor = operations();
        Version expected = Objects.requireNonNull(aggregate.version(), "aggregate version must not be null");
        Version persisted = new Version(Math.addExact(expected.value(), 1));
        int affected = expected.value() == 0
                ? executor.insert(aggregate, persisted)
                : executor.update(aggregate, expected, persisted);
        checkConcurrency(affected, aggregate.id().value(), expected.value());
        versionSynchronizer.synchronize(aggregate, expected, persisted);
        return aggregate;
    }

    @Override
    public void delete(A aggregate) {
        Objects.requireNonNull(aggregate, "aggregate must not be null");
        Version expected = Objects.requireNonNull(aggregate.version(), "aggregate version must not be null");
        checkConcurrency(operations().delete(aggregate, expected), aggregate.id().value(), expected.value());
    }

    private AggregatePersistenceOperations<A, I> operations() {
        if (operations == null) {
            throw new UnsupportedOperationException("仓储未配置 SQL 执行策略");
        }
        return operations;
    }

    /**
     * Checks the update result for optimistic concurrency.
     *
     * <p>Phase XII §11: if the UPDATE affected zero rows, a concurrency
     * conflict has occurred.</p>
     *
     * @param affectedRows    the number of rows affected by the UPDATE
     * @param aggregateId     the aggregate identifier
     * @param expectedVersion the version that was expected
     * @throws MyBatisConcurrencyException if affectedRows != 1
     */
    protected void checkConcurrency(int affectedRows, String aggregateId, long expectedVersion) {
        if (affectedRows != 1) {
            throw new MyBatisConcurrencyException(aggregateId, expectedVersion);
        }
    }

    /**
     * Returns the session adapter for subclass use.
     */
    protected MyBatisSessionAdapter sessionAdapter() {
        if (sessionAdapter == null) {
            throw new UnsupportedOperationException("执行策略负责 session 生命周期");
        }
        return sessionAdapter;
    }

    /**
     * Returns the aggregate mapper for subclass use.
     */
    protected AggregateMapper<A, ?> mapper() {
        if (mapper == null) {
            throw new UnsupportedOperationException("执行策略负责领域映射");
        }
        return mapper;
    }
}
