package io.github.regalpine.ddd.conformance;

import io.github.regalpine.ddd.core.error.ErrorCategory;
import io.github.regalpine.ddd.core.error.FrameworkError;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Conformance test suite for Phase X Developer Framework Implementation.
 *
 * <p>Validates that all Phase X required types exist and satisfy their contracts:
 * FrameworkError, ErrorCategory, CompatibilityLevel, and In-Memory adapter types.</p>
 *
 * @author RegalPine
 */
public abstract class PhaseXConformance {

    /**
     * Verifies FrameworkError interface exists and can be implemented (§58).
     */
    @Test
    public void verifyFrameworkError() {
        FrameworkError error = new FrameworkError() {
            @Override
            public String code() { return "TEST_001"; }

            @Override
            public String message() { return "Test error"; }
        };
        assertEquals("TEST_001", error.code(), "FrameworkError.code() should return stable code");
        assertEquals("Test error", error.message(), "FrameworkError.message() should return message");
    }

    /**
     * Verifies ErrorCategory enum has all required values (§58).
     */
    @Test
    public void verifyErrorCategory() {
        ErrorCategory[] categories = ErrorCategory.values();
        assertEquals(8, categories.length, "ErrorCategory should have 8 values");
        // Verify all required categories exist
        for (String required : new String[]{
                "DOMAIN", "APPLICATION", "CONCURRENCY", "TRANSACTION",
                "INFRASTRUCTURE", "MESSAGING", "CONFIGURATION", "RUNTIME"}) {
            assertDoesNotThrow(() -> ErrorCategory.valueOf(required),
                    "Missing ErrorCategory: " + required);
        }
    }

    /**
     * Verifies CompatibilityLevel enum has all required values (§50).
     */
    @Test
    public void verifyCompatibilityLevel() {
        CompatibilityLevel[] levels = CompatibilityLevel.values();
        assertEquals(5, levels.length, "CompatibilityLevel should have 5 values");
        for (String required : new String[]{
                "CORE_COMPATIBLE", "RUNTIME_COMPATIBLE", "PERSISTENCE_COMPATIBLE",
                "MESSAGING_COMPATIBLE", "FULLY_CONFORMANT"}) {
            assertDoesNotThrow(() -> CompatibilityLevel.valueOf(required),
                    "Missing CompatibilityLevel: " + required);
        }
    }

    /**
     * Verifies In-Memory types exist (§33).
     */
    @Test
    public void verifyInMemoryTypesExist() {
        assertDoesNotThrow(
                () -> Class.forName("io.github.regalpine.ddd.infrastructure.persistence.InMemoryRepository"),
                "Missing required In-Memory type: InMemoryRepository");
        assertDoesNotThrow(
                () -> Class.forName("io.github.regalpine.ddd.infrastructure.event.InMemoryEventStore"),
                "Missing required In-Memory type: InMemoryEventStore");
        assertDoesNotThrow(
                () -> Class.forName("io.github.regalpine.ddd.infrastructure.messaging.InMemoryMessageBus"),
                "Missing required In-Memory type: InMemoryMessageBus");
        assertDoesNotThrow(
                () -> Class.forName("io.github.regalpine.ddd.infrastructure.transaction.InMemoryTransactionAdapter"),
                "Missing required In-Memory type: InMemoryTransactionAdapter");
        assertDoesNotThrow(
                () -> Class.forName("io.github.regalpine.ddd.infrastructure.inbox.InMemoryInboxStore"),
                "Missing required In-Memory type: InMemoryInboxStore");
        assertDoesNotThrow(
                () -> Class.forName("io.github.regalpine.ddd.infrastructure.outbox.InMemoryOutboxStore"),
                "Missing required In-Memory type: InMemoryOutboxStore");
    }

    /**
     * Verifies HandlerRegistry unified interface exists (§17).
     */
    @Test
    public void verifyHandlerRegistryExists() {
        assertDoesNotThrow(() -> {
            Class<?> clazz = Class.forName("io.github.regalpine.ddd.cqrs.registry.HandlerRegistry");
            clazz.getMethod("registerCommand", Class.class,
                    io.github.regalpine.ddd.application.command.CommandHandler.class);
            clazz.getMethod("registerQuery", Class.class,
                    io.github.regalpine.ddd.application.query.QueryHandler.class);
        }, "HandlerRegistry missing or incomplete");
    }
}
