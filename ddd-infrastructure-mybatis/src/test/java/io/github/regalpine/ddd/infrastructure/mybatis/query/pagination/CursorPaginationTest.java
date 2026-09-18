package io.github.regalpine.ddd.infrastructure.mybatis.query.pagination;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CursorPagination - validation (§35)")
class CursorPaginationTest {

    @Test
    @DisplayName("valid cursor and size")
    void validCursor() {
        var pag = new CursorPagination("abc123", 50);
        assertEquals("abc123", pag.cursor());
        assertEquals(50, pag.size());
    }

    @Test
    @DisplayName("zero size throws IllegalArgumentException")
    void zeroSize() {
        assertThrows(IllegalArgumentException.class, () -> new CursorPagination("abc", 0));
    }

    @Test
    @DisplayName("negative size throw IllegalArgumentException")
    void negativeSize() {
        assertThrows(IllegalArgumentException.class, () -> new CursorPagination("abc", -1));
    }

    @Test
    @DisplayName("CursorPagination implements Pagination")
    void sealedHierarchy() {
        Pagination pag = new CursorPagination("cursor", 10);
        assertInstanceOf(Pagination.class, pag);
    }

    @Test
    @DisplayName("OffsetPagination implements Pagination")
    void offsetPaginationHierarchy() {
        Pagination pag = new OffsetPagination(0, 20);
        assertInstanceOf(Pagination.class, pag);
    }
}
