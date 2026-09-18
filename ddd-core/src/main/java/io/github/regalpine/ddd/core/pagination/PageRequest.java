package io.github.regalpine.ddd.core.pagination;

import java.util.List;

/**
 * A request for a page of results with sorting and count strategy.
 *
 * <p>Phase XII §16: unified pagination request used by all query-side
 * repository implementations. The framework computes the database offset
 * from {@link #offset()} so that individual mappers need not calculate it.</p>
 *
 * @param page      the zero-based page number, must be &gt;= 0
 * @param size      the page size, must be &gt; 0
 * @param sorts     the sort orders (immutable, never null)
 * @param countMode the count strategy (defaults to {@link CountMode#EXACT})
 * @author RegalPine
 */
public record PageRequest(
        int page,
        int size,
        List<SortOrder> sorts,
        CountMode countMode
) {

    public PageRequest {
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be > 0");
        }
        sorts = sorts == null ? List.of() : List.copyOf(sorts);
        countMode = countMode == null ? CountMode.EXACT : countMode;
    }

    /**
     * Returns the database offset computed from page and size.
     *
     * @return {@code (long) page * size}
     */
    public long offset() {
        return (long) page * size;
    }
}
