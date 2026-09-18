package io.github.regalpine.ddd.infrastructure.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link PersistenceException} hierarchy (Phase VII §62).
 */
@DisplayName("PersistenceException hierarchy")
class PersistenceExceptionTest {

    @Test
    @DisplayName("all subtypes extend PersistenceException which extends InfrastructureException")
    void hierarchyIsCorrect() {
        assertInstanceOf(InfrastructureException.class, new PersistenceAccessException("test"));
        assertInstanceOf(InfrastructureException.class, new PersistenceMappingException("test"));
        assertInstanceOf(InfrastructureException.class,
                new PersistenceConstraintException("test", PersistenceConstraintException.ConstraintType.UNIQUE));
        assertInstanceOf(InfrastructureException.class, new PersistenceConnectionException("test"));
        assertInstanceOf(InfrastructureException.class, new PersistenceTimeoutException("test"));

        assertInstanceOf(PersistenceException.class, new PersistenceAccessException("test"));
        assertInstanceOf(PersistenceException.class, new PersistenceMappingException("test"));
        assertInstanceOf(PersistenceException.class,
                new PersistenceConstraintException("test", PersistenceConstraintException.ConstraintType.FK));
        assertInstanceOf(PersistenceException.class, new PersistenceConnectionException("test"));
        assertInstanceOf(PersistenceException.class, new PersistenceTimeoutException("test"));
    }

    @Test
    @DisplayName("message and cause are preserved")
    void messageAndCausePreserved() {
        var cause = new RuntimeException("root cause");
        var ex = new PersistenceAccessException("access failed", cause);

        assertEquals("access failed", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    @DisplayName("PersistenceConstraintException carries constraint type")
    void constraintTypeCarried() {
        for (var type : PersistenceConstraintException.ConstraintType.values()) {
            var ex = new PersistenceConstraintException("violation: " + type, type);
            assertEquals(type, ex.constraintType());
        }
    }

    @Test
    @DisplayName("PersistenceConstraintException rejects null constraint type")
    void constraintTypeNullRejected() {
        assertThrows(NullPointerException.class,
                () -> new PersistenceConstraintException("msg", null));
    }
}
