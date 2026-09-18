package io.github.regalpine.ddd.infrastructure.mybatis.query.pagination;

/**
 * Cursor-based (keyset) pagination.
 *
 * <p>Phase XIII §35: validates size > 0. The cursor is an opaque string
 * that encodes the position for the next page.</p>
 *
 * @param cursor the cursor position (opaque string)
 * @param size   the page size
 * @author RegalPine
 */
public record CursorPagination(
        String cursor,
        int size
) implements Pagination {

    public CursorPagination {
        if (size <= 0) {
            throw new IllegalArgumentException("size must be > 0");
        }
    }
}
