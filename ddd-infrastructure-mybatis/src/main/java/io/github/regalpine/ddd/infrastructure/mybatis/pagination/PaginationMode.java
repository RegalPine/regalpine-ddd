package io.github.regalpine.ddd.infrastructure.mybatis.pagination;

/**
 * Pagination strategy mode.
 *
 * <p>Phase XII §39: controls whether offset or cursor pagination is used.
 * AUTO lets the framework choose based on adapter capabilities and query
 * characteristics — but never silently changes user semantics (§40).</p>
 *
 * @author RegalPine
 */
public enum PaginationMode {

    /**
     * Offset-based pagination (traditional LIMIT/OFFSET).
     */
    OFFSET,

    /**
     * Cursor/keyset-based pagination (WHERE id > ?).
     */
    CURSOR,

    /**
     * Framework chooses the best strategy automatically.
     */
    AUTO
}
