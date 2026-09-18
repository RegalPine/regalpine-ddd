package io.github.regalpine.ddd.domain.factory;

import io.github.regalpine.ddd.core.aggregate.AggregateRootSupport;
import io.github.regalpine.ddd.core.identifier.Identifier;
import io.github.regalpine.ddd.core.version.Version;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainFactoryTest {

    // -- Test fixtures --

    record TestId(String value) implements Identifier {
        @Override
        public String value() {
            return value;
        }
    }

    static class TestAggregate extends AggregateRootSupport<TestId> {
        private final TestId id;
        private final String name;
        private Version version;

        TestAggregate(TestId id, String name) {
            this.id = id;
            this.name = name;
            this.version = Version.initial();
        }

        @Override
        public TestId id() {
            return id;
        }

        @Override
        public Version version() {
            return version;
        }

        String name() {
            return name;
        }
    }

    record CreateTestData(String id, String name) {}

    static final class TestAggregateFactory implements DomainFactory<CreateTestData, TestAggregate> {
        @Override
        public TestAggregate create(CreateTestData input) {
            if (input.name() == null || input.name().isBlank()) {
                throw new IllegalArgumentException("Name must not be blank");
            }
            return new TestAggregate(new TestId(input.id()), input.name());
        }
    }

    // -- Tests --

    @Test
    void shouldCreateAggregateFromInput() {
        DomainFactory<CreateTestData, TestAggregate> factory = new TestAggregateFactory();
        var aggregate = factory.create(new CreateTestData("agg-1", "Test"));

        assertThat(aggregate.id().value()).isEqualTo("agg-1");
        assertThat(aggregate.name()).isEqualTo("Test");
    }

    @Test
    void shouldRejectBlankName() {
        DomainFactory<CreateTestData, TestAggregate> factory = new TestAggregateFactory();

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> factory.create(new CreateTestData("agg-1", ""))
        ).isInstanceOf(IllegalArgumentException.class);
    }
}
