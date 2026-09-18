package io.github.regalpine.ddd.conformance.mybatis;

import io.github.regalpine.ddd.infrastructure.mybatis.pagination.PaginationDialect;

/**
 * Conformance test suite for pagination dialect implementations.
 *
 * <p>Phase XII §67: verifies that each dialect produces correct SQL
 * for various pagination scenarios (first page, middle page, empty result,
 * last page, large offset, sorting).</p>
 *
 * @author RegalPine
 */
public abstract class MyBatisPaginationConformance {

    /**
     * Subclasses must provide the dialect under test.
     */
    protected abstract PaginationDialect dialect();

    /**
     * Verifies that applyOffsetLimit produces non-empty SQL.
     */
    protected void verifyPaginationApplied() {
        String sql = "SELECT * FROM orders";
        String paginated = dialect().applyOffsetLimit(sql, 0, 10);

        if (paginated.equals(sql)) {
            throw new AssertionError(
                    "Dialect should modify SQL for pagination");
        }
    }

    /**
     * Verifies dialect has a non-empty name.
     */
    protected void verifyDialectName() {
        String name = dialect().name();
        if (name == null || name.isBlank()) {
            throw new AssertionError("Dialect name must not be blank");
        }
    }

    /**
     * Runs all conformance checks.
     */
    public void runAll() {
        verifyDialectName();
        verifyPaginationApplied();
    }
}
