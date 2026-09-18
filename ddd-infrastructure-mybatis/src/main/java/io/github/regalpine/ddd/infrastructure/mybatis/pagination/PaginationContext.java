package io.github.regalpine.ddd.infrastructure.mybatis.pagination;

import io.github.regalpine.ddd.core.pagination.PageRequest;

/**
 * Internal pagination context passed to the MyBatis interceptor.
 *
 * <p>Phase XII §31: the framework query executor sets this context
 * so that the interceptor can apply dialect-specific SQL rewriting
 * without individual mappers computing offsets.</p>
 *
 * @param request the page request
 * @author RegalPine
 */
public record PaginationContext(
        PageRequest request
) {
}
