package io.github.regalpine.ddd.conformance.mybatis.query;

import io.github.regalpine.ddd.core.pagination.CountMode;
import io.github.regalpine.ddd.infrastructure.mybatis.query.pagination.CursorPagination;
import io.github.regalpine.ddd.infrastructure.mybatis.query.pagination.OffsetPagination;
import io.github.regalpine.ddd.infrastructure.mybatis.query.pagination.Pagination;
import io.github.regalpine.ddd.infrastructure.mybatis.query.wrapper.Wrappers;

/**
 * Phase XIII §76: conformance for pagination and dialect integration.
 *
 * <p>Verifies offset calculation, overflow detection, cursor validation,
 * CountMode support, and the sealed Pagination hierarchy.</p>
 *
 * @author RegalPine
 */
public final class QueryPaginationConformance {

    private QueryPaginationConformance() {
    }

    /**
     * PAGE-001: Pagination request validates page/size.
     */
    public static boolean validatesPageAndSize() {
        try {
            new OffsetPagination(-1, 20);
            return false;
        } catch (IllegalArgumentException e) {
            try {
                new OffsetPagination(0, 0);
                return false;
            } catch (IllegalArgumentException e2) {
                return true;
            }
        }
    }

    /**
     * PAGE-002: Offset calculation detects overflow.
     */
    public static boolean overflowDetection() {
        var pag = new OffsetPagination(Long.MAX_VALUE, 2);
        try {
            pag.offset();
            return false;
        } catch (ArithmeticException e) {
            return true;
        }
    }

    /**
     * §34: Offset calculation is correct.
     */
    public static boolean correctOffset() {
        return new OffsetPagination(0, 20).offset() == 0
                && new OffsetPagination(1, 20).offset() == 20
                && new OffsetPagination(10, 50).offset() == 500;
    }

    /**
     * §35: Cursor validates size > 0.
     */
    public static boolean cursorValidatesSize() {
        try {
            new CursorPagination("abc", 0);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    /**
     * §33: Pagination is sealed (Offset or Cursor only).
     */
    public static boolean sealedPagination() {
        return Pagination.class.isSealed()
                && OffsetPagination.class.isAssignableFrom(OffsetPagination.class)
                && CursorPagination.class.isAssignableFrom(CursorPagination.class);
    }

    /**
     * §47: CountMode has all required values.
     */
    public static boolean countModeComplete() {
        return CountMode.EXACT != null
                && CountMode.NONE != null
                && CountMode.ESTIMATED != null
                && CountMode.AUTO != null;
    }

    /**
     * §36: Wrapper preserves pagination in plan.
     */
    public static boolean wrapperPreservesPagination() {
        var wrapper = Wrappers.<Object>query().page(0, 20);
        var plan = wrapper.freeze();
        return plan.pagination() != null
                && plan.pagination() instanceof OffsetPagination;
    }
}
