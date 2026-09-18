package io.github.regalpine.ddd.infrastructure.mybatis.query.pagination;

/**
 * Offset-based pagination with page number and size.
 *
 * <p>Phase XIII §34: validates page >= 0 and size > 0.
 * The {@link #offset()} method uses {@link Math#multiplyExact} to detect overflow.</p>
 *
 * @param page the zero-based page number
 * @param size the page size
 * @author RegalPine
 */
public record OffsetPagination(
        long page,
        long size
) implements Pagination {

    public OffsetPagination {
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be > 0");
        }
    }

    /**
     * Computes the offset for this page.
     *
     * @return the zero-based offset
     * @throws ArithmeticException if the computation overflows
     */
    public long offset() {
        return Math.multiplyExact(page, size);
    }
}
