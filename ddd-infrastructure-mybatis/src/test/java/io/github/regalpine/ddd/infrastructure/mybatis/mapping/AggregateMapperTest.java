package io.github.regalpine.ddd.infrastructure.mybatis.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link AggregateMapper} interface contract (Phase XII §10).
 */
@DisplayName("AggregateMapper")
class AggregateMapperTest {

    record TestAggregate(String id, String name) {}
    record TestRecord(String id, String name) {}

    @Test
    @DisplayName("toRecord and toAggregate are inverse operations")
    void roundTrip() {
        AggregateMapper<TestAggregate, TestRecord> mapper = new AggregateMapper<>() {
            @Override
            public TestRecord toRecord(TestAggregate aggregate) {
                return new TestRecord(aggregate.id(), aggregate.name());
            }

            @Override
            public TestAggregate toAggregate(TestRecord record) {
                return new TestAggregate(record.id(), record.name());
            }
        };

        var original = new TestAggregate("id-1", "Test");
        var record = mapper.toRecord(original);
        var restored = mapper.toAggregate(record);

        assertEquals(original, restored);
    }

    @Test
    @DisplayName("mapper preserves identity")
    void preservesIdentity() {
        AggregateMapper<TestAggregate, TestRecord> mapper = new AggregateMapper<>() {
            @Override
            public TestRecord toRecord(TestAggregate aggregate) {
                return new TestRecord(aggregate.id(), aggregate.name());
            }

            @Override
            public TestAggregate toAggregate(TestRecord record) {
                return new TestAggregate(record.id(), record.name());
            }
        };

        var original = new TestAggregate("unique-id", "Name");
        var restored = mapper.toAggregate(mapper.toRecord(original));

        assertEquals("unique-id", restored.id());
    }
}
