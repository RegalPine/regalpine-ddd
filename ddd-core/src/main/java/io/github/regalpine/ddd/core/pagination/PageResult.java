package io.github.regalpine.ddd.core.pagination;

import java.util.List;
import java.util.Objects;

/**
 * An immutable page of results for pagination queries.
 *
 * <p>Per Phase XII §19, the unified framework pagination result includes
 * computed navigation fields (totalPages, first, last, hasNext, hasPrevious).
 * Use {@link #of(List, long, long, long)} for standard construction.</p>
 *
 * <p>When {@code CountMode.NONE} is used, {@code totalElements} and
 * {@code totalPages} are set to {@code -1} (Phase XII §35).</p>
 *
 * @param <T> the element type
 * @author RegalPine
 */
public record PageResult<T>(
        List<T> content,
        long page,
        long size,
        long totalElements,
        long totalPages,
        boolean first,
        boolean last,
        boolean hasNext,
        boolean hasPrevious
) {

    public PageResult {
        Objects.requireNonNull(content, "content must not be null");
        content = List.copyOf(content);
    }

    /**
     * Factory method that computes navigation fields from page, size, and totalElements.
     *
     * @param content       the items on this page
     * @param page          the zero-based page number
     * @param size          the page size
     * @param totalElements the total number of elements (-1 if unknown)
     * @param <T>           the element type
     * @return a new page result with computed navigation fields
     */
    public static <T> PageResult<T> of(
            List<T> content,
            long page,
            long size,
            long totalElements) {

        long totalPages = size == 0
                ? 0
                : (totalElements < 0
                        ? -1
                        : (totalElements + size - 1) / size);

        return new PageResult<>(
                content,
                page,
                size,
                totalElements,
                totalPages,
                page == 0,
                totalElements < 0 ? false : page + 1 >= totalPages,
                totalElements < 0 ? true : page + 1 < totalPages,
                page > 0
        );
    }
}
