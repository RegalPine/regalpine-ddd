package io.github.regalpine.ddd.core.pagination;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link PageRequest} record (Phase XII §16).
 */
@DisplayName("PageRequest")
class PageRequestTest {

    @Test
    @DisplayName("valid construction succeeds")
    void validConstruction() {
        var req = new PageRequest(0, 20, List.of(), CountMode.EXACT);

        assertEquals(0, req.page());
        assertEquals(20, req.size());
        assertEquals(List.of(), req.sorts());
        assertEquals(CountMode.EXACT, req.countMode());
    }

    @Test
    @DisplayName("offset is computed correctly")
    void offsetComputation() {
        assertEquals(0, new PageRequest(0, 10, null, null).offset());
        assertEquals(30, new PageRequest(3, 10, null, null).offset());
        assertEquals(200L * 50, new PageRequest(200, 50, null, null).offset());
    }

    @Test
    @DisplayName("null sorts defaults to empty list")
    void nullSortsDefaultsToEmpty() {
        var req = new PageRequest(0, 10, null, null);

        assertNotNull(req.sorts());
        assertTrue(req.sorts().isEmpty());
    }

    @Test
    @DisplayName("null countMode defaults to EXACT")
    void nullCountModeDefaultsToExact() {
        var req = new PageRequest(0, 10, null, null);

        assertEquals(CountMode.EXACT, req.countMode());
    }

    @Test
    @DisplayName("sorts list is defensively copied")
    void sortsAreCopied() {
        var sorts = new java.util.ArrayList<>(List.of(
                new SortOrder("name", SortOrder.Direction.ASC)));
        var req = new PageRequest(0, 10, sorts, null);

        sorts.add(new SortOrder("id", SortOrder.Direction.DESC));
        assertEquals(1, req.sorts().size());
    }

    @Test
    @DisplayName("rejects negative page")
    void rejectsNegativePage() {
        assertThrows(IllegalArgumentException.class,
                () -> new PageRequest(-1, 10, null, null));
    }

    @Test
    @DisplayName("rejects zero size")
    void rejectsZeroSize() {
        assertThrows(IllegalArgumentException.class,
                () -> new PageRequest(0, 0, null, null));
    }

    @Test
    @DisplayName("rejects negative size")
    void rejectsNegativeSize() {
        assertThrows(IllegalArgumentException.class,
                () -> new PageRequest(0, -5, null, null));
    }
}
