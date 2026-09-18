package io.github.regalpine.ddd.infrastructure.mybatis.pagination;

/**
 * Resolves the appropriate {@link PaginationDialect} for a given database.
 *
 * <p>Phase XII §27: at startup, the adapter uses JDBC metadata to determine
 * the database type and selects the matching dialect.</p>
 *
 * @author RegalPine
 */
public interface PaginationDialectResolver {

    /**
     * Resolves a pagination dialect for the given database metadata.
     *
     * @param metadata the database metadata
     * @return the matching dialect
     * @throws IllegalStateException if the database is not supported
     */
    PaginationDialect resolve(DatabaseMetadata metadata);
}
