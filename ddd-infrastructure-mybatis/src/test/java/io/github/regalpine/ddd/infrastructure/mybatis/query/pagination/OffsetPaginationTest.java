package io.github.regalpine.ddd.infrastructure.mybatis.query.pagination;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OffsetPagination - offset calculation + overflow (§34, §72)")
class OffsetPaginationTest {

    @Test
    @DisplayName("page=0 size=20 → offset=0")
    void firstPage() {
        var pag = new OffsetPagination(0, 20);
        assertEquals(0, pag.offset());
    }

    @Test
    @DisplayName("page=1 size=20 → offset=20")
    void secondPage() {
        var pag = new OffsetPagination(1, 20);
        assertEquals(20, pag.offset());
    }

    @Test
    @DisplayName("page=10 size=50 → offset=500")
    void tenthPage() {
        var pag = new OffsetPagination(10, 50);
        assertEquals(500, pag.offset());
    }

    @Test
    @DisplayName("negative page throws IllegalArgumentException")
    void negativePage() {
        assertThrows(IllegalArgumentException.class, () -> new OffsetPagination(-1, 20));
    }

    @Test
    @DisplayName("zero size throws IllegalArgumentException")
    void zeroSize() {
        assertThrows(IllegalArgumentException.class, () -> new OffsetPagination(0, 0));
    }

    @Test
    @DisplayName("negative size throws IllegalArgumentException")
    void negativeSize() {
        assertThrows(IllegalArgumentException.class, () -> new OffsetPagination(0, -1));
    }

    @Test
    @DisplayName("overflow detection via Math.multiplyExact")
    void overflowDetection() {
        var pag = new OffsetPagination(Long.MAX_VALUE, 2);
        assertThrows(ArithmeticException.class, pag::offset);
    }
}
