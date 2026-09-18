package io.github.regalpine.ddd.infrastructure.mybatis.repository;

import io.github.regalpine.ddd.core.aggregate.AggregateRootSupport;
import io.github.regalpine.ddd.core.identifier.Identifier;
import io.github.regalpine.ddd.core.version.Version;
import io.github.regalpine.ddd.infrastructure.mybatis.exception.MyBatisConcurrencyException;
import io.github.regalpine.ddd.infrastructure.mybatis.mapping.AggregateMapper;
import io.github.regalpine.ddd.infrastructure.mybatis.session.MyBatisSessionAdapter;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for optimistic concurrency check logic (Phase XII §11).
 */
@DisplayName("MyBatisAggregateRepository")
class MyBatisAggregateRepositoryTest {

    @Test
    @DisplayName("MyBatisConcurrencyException carries aggregate ID and version")
    void concurrencyExceptionCarriesDetails() {
        var ex = new MyBatisConcurrencyException("agg-1", 5L);

        assertEquals("agg-1", ex.aggregateId());
        assertEquals(5L, ex.expectedVersion());
        assertTrue(ex.getMessage().contains("agg-1"));
        assertTrue(ex.getMessage().contains("5"));
    }

    @Test
    @DisplayName("MyBatisConcurrencyException extends MyBatisAdapterException")
    void concurrencyExceptionHierarchy() {
        var ex = new MyBatisConcurrencyException("agg-1", 5L);
        assertInstanceOf(
                io.github.regalpine.ddd.infrastructure.mybatis.exception.MyBatisAdapterException.class,
                ex);
    }

    @Test
    @DisplayName("MyBatisAggregateRepository implements AggregateRepository")
    void repositoryImplementsContract() {
        // Structural check: verify the class implements the right interface
        assertTrue(
                io.github.regalpine.ddd.domain.repository.AggregateRepository.class
                        .isAssignableFrom(MyBatisAggregateRepository.class),
                "MyBatisAggregateRepository must implement AggregateRepository");
    }

    @Test
    void insertThenUpdateSynchronizesSameInstance() {
        var operations = new Operations();
        var repository = repository(operations);
        var aggregate = new TestAggregate(0);
        assertSame(aggregate, repository.save(aggregate));
        assertEquals(new Version(1), aggregate.version());
        assertEquals(1, operations.inserts);
        assertEquals(0, operations.updates);
        assertSame(aggregate, repository.save(aggregate));
        assertEquals(new Version(2), aggregate.version());
        assertEquals(1, operations.inserts);
        assertEquals(1, operations.updates);
        assertEquals(new Version(1), operations.expected);
        assertEquals(new Version(2), operations.persisted);
    }

    @Test
    void missingUpdateNeverFallsBackToInsertOrChangesVersion() {
        var operations = new Operations();
        operations.affected = 0;
        var aggregate = new TestAggregate(7);
        var error = assertThrows(MyBatisConcurrencyException.class,
                () -> repository(operations).save(aggregate));
        assertEquals(7, error.expectedVersion());
        assertEquals(0, operations.inserts);
        assertEquals(1, operations.updates);
        assertEquals(new Version(7), aggregate.version());
    }

    @Test
    void unexpectedRowCountDoesNotSynchronizeVersion() {
        var operations = new Operations();
        operations.affected = 2;
        var aggregate = new TestAggregate(0);
        assertThrows(MyBatisConcurrencyException.class, () -> repository(operations).save(aggregate));
        assertEquals(Version.initial(), aggregate.version());
    }

    @Test
    void databaseConstraintAndAssociationFailuresAreNotReclassified() {
        var operations = new Operations();
        operations.failure = new IllegalStateException("数据库约束或关联写入失败");
        var aggregate = new TestAggregate(0);
        assertSame(operations.failure,
                assertThrows(IllegalStateException.class, () -> repository(operations).save(aggregate)));
        assertEquals(Version.initial(), aggregate.version());
    }

    @Test
    void findDelegatesAndPreservesMissingResult() {
        var operations = new Operations();
        var repository = repository(operations);
        var id = new TestId("test");
        assertTrue(repository.findById(id).isEmpty());
        operations.found = Optional.of(new TestAggregate(4));
        assertSame(operations.found.get(), repository.findById(id).orElseThrow());
        assertEquals(id, operations.queriedId);
    }

    @Test
    void deleteChecksAffectedRowsWithoutChangingVersion() {
        var operations = new Operations();
        var aggregate = new TestAggregate(2);
        repository(operations).delete(aggregate);
        assertEquals(new Version(2), operations.expected);
        assertEquals(new Version(2), aggregate.version());
        operations.affected = 0;
        assertThrows(MyBatisConcurrencyException.class, () -> repository(operations).delete(aggregate));
    }

    @Test
    void unsupportedDeleteRemainsExplicit() {
        var operations = new Operations();
        operations.failure = new UnsupportedOperationException("安全对象不得通用删除");
        assertThrows(UnsupportedOperationException.class,
                () -> repository(operations).delete(new TestAggregate(1)));
    }

    @Test
    void versionOverflowFailsBeforeSqlExecution() {
        var operations = new Operations();
        assertThrows(ArithmeticException.class,
                () -> repository(operations).save(new TestAggregate(Long.MAX_VALUE)));
        assertEquals(0, operations.inserts + operations.updates);
    }

    @Test
    @SuppressWarnings("deprecation")
    void legacyConstructorCannotSilentlyExecuteCrud() {
        var session = new MyBatisSessionAdapter(new SqlSessionFactoryBuilder().build(new Configuration()));
        AggregateMapper<TestAggregate, TestAggregate> mapper = new AggregateMapper<>() {
            @Override public TestAggregate toRecord(TestAggregate aggregate) { return aggregate; }
            @Override public TestAggregate toAggregate(TestAggregate record) { return record; }
        };
        var repository = new MyBatisAggregateRepository<TestAggregate, TestId>(session, mapper);
        assertThrows(UnsupportedOperationException.class, () -> repository.findById(new TestId("test")));
        assertThrows(UnsupportedOperationException.class, () -> repository.save(new TestAggregate(0)));
        assertThrows(UnsupportedOperationException.class, () -> repository.delete(new TestAggregate(1)));
        assertFalse(session.hasCurrentSession());
    }

    @Test
    void rejectsNullInputs() {
        var repository = repository(new Operations());
        assertThrows(NullPointerException.class, () -> repository.findById(null));
        assertThrows(NullPointerException.class, () -> repository.save(null));
        assertThrows(NullPointerException.class, () -> repository.delete(null));
        assertThrows(NullPointerException.class,
                () -> new MyBatisAggregateRepository<TestAggregate, TestId>(new Operations(), null));
    }

    private static MyBatisAggregateRepository<TestAggregate, TestId> repository(Operations operations) {
        return new MyBatisAggregateRepository<>(operations, (aggregate, expected, persisted) -> {
            assertEquals(expected, aggregate.version());
            aggregate.version = persisted;
        });
    }

    private record TestId(String value) implements Identifier {}

    private static final class TestAggregate extends AggregateRootSupport<TestId> {
        private Version version;
        private TestAggregate(long version) { this.version = new Version(version); }
        @Override public TestId id() { return new TestId("test"); }
        @Override public Version version() { return version; }
    }

    private static final class Operations implements AggregatePersistenceOperations<TestAggregate, TestId> {
        private int inserts;
        private int updates;
        private int affected = 1;
        private Version expected;
        private Version persisted;
        private TestId queriedId;
        private RuntimeException failure;
        private Optional<TestAggregate> found = Optional.empty();

        @Override
        public Optional<TestAggregate> findById(TestId id) {
            queriedId = id;
            return found;
        }

        @Override
        public int insert(TestAggregate aggregate, Version persistedVersion) {
            inserts++;
            persisted = persistedVersion;
            return result();
        }

        @Override
        public int update(TestAggregate aggregate, Version expectedVersion, Version persistedVersion) {
            updates++;
            expected = expectedVersion;
            persisted = persistedVersion;
            return result();
        }

        @Override
        public int delete(TestAggregate aggregate, Version expectedVersion) {
            expected = expectedVersion;
            return result();
        }

        private int result() {
            if (failure != null) throw failure;
            return affected;
        }
    }
}
