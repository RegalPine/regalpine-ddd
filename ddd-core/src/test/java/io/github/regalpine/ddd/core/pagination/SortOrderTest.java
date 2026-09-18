package io.github.regalpine.ddd.core.pagination;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link SortOrder} record (Phase XII §17).
 */
@DisplayName("SortOrder")
class SortOrderTest {

    @Test
    @DisplayName("valid construction succeeds")
    void validConstruction() {
        var sort = new SortOrder("createdAt", SortOrder.Direction.DESC);

        assertEquals("createdAt", sort.property());
        assertEquals(SortOrder.Direction.DESC, sort.direction());
    }

    @Test
    @DisplayName("rejects blank property")
    void rejectsBlankProperty() {
        assertThrows(IllegalArgumentException.class,
                () -> new SortOrder("", SortOrder.Direction.ASC));
        assertThrows(IllegalArgumentException.class,
                () -> new SortOrder("   ", SortOrder.Direction.ASC));
    }

    @Test
    @DisplayName("rejects null property")
    void rejectsNullProperty() {
        assertThrows(IllegalArgumentException.class,
                () -> new SortOrder(null, SortOrder.Direction.ASC));
    }

    @Test
    @DisplayName("rejects null direction")
    void rejectsNullDirection() {
        assertThrows(NullPointerException.class,
                () -> new SortOrder("name", null));
    }

    @Test
    @DisplayName("Direction enum has ASC and DESC")
    void directionEnumValues() {
        assertEquals(2, SortOrder.Direction.values().length);
        assertNotNull(SortOrder.Direction.valueOf("ASC"));
        assertNotNull(SortOrder.Direction.valueOf("DESC"));
    }
}
