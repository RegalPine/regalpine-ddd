package io.github.regalpine.ddd.core.pagination;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link PageResult} record (Phase XII §19).
 */
@DisplayName("PageResult")
class PageResultTest {

    @Test
    @DisplayName("of() computes navigation fields correctly")
    void factoryMethodComputesFields() {
        var page = PageResult.of(List.of("a", "b"), 0, 2, 5);

        assertEquals(List.of("a", "b"), page.content());
        assertEquals(0, page.page());
        assertEquals(2, page.size());
        assertEquals(5, page.totalElements());
        assertEquals(3, page.totalPages());
        assertTrue(page.first());
        assertFalse(page.last());
        assertTrue(page.hasNext());
        assertFalse(page.hasPrevious());
    }

    @Test
    @DisplayName("last page has correct navigation flags")
    void lastPage() {
        var page = PageResult.of(List.of("e"), 2, 2, 5);

        assertEquals(3, page.totalPages());
        assertFalse(page.first());
        assertTrue(page.last());
        assertFalse(page.hasNext());
        assertTrue(page.hasPrevious());
    }

    @Test
    @DisplayName("middle page has correct navigation flags")
    void middlePage() {
        var page = PageResult.of(List.of("c", "d"), 1, 2, 5);

        assertEquals(3, page.totalPages());
        assertFalse(page.first());
        assertFalse(page.last());
        assertTrue(page.hasNext());
        assertTrue(page.hasPrevious());
    }

    @Test
    @DisplayName("empty result has zero totalPages")
    void emptyResult() {
        var empty = PageResult.of(List.of(), 0, 10, 0);

        assertEquals(0, empty.totalPages());
        assertTrue(empty.first());
        assertTrue(empty.last());
        assertFalse(empty.hasNext());
        assertFalse(empty.hasPrevious());
    }

    @Test
    @DisplayName("negative totalElements indicates unknown count (CountMode.NONE)")
    void unknownCount() {
        var page = PageResult.of(List.of("a", "b"), 0, 20, -1);

        assertEquals(-1, page.totalElements());
        assertEquals(-1, page.totalPages());
        assertTrue(page.first());
        assertFalse(page.last());
        assertTrue(page.hasNext());
        assertFalse(page.hasPrevious());
    }

    @Test
    @DisplayName("content is defensively copied")
    void contentIsCopied() {
        var original = new java.util.ArrayList<>(List.of("x"));
        var page = PageResult.of(original, 0, 10, 1);

        original.add("y");
        assertEquals(1, page.content().size());
    }

    @Test
    @DisplayName("null content is rejected")
    void rejectsNullContent() {
        assertThrows(NullPointerException.class,
                () -> PageResult.of(null, 0, 10, 0));
    }
}
