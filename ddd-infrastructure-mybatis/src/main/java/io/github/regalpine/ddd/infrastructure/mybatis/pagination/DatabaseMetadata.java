package io.github.regalpine.ddd.infrastructure.mybatis.pagination;

/**
 * Database product metadata for dialect resolution.
 *
 * <p>Phase XII §27: derived from JDBC {@code DatabaseMetaData} at startup.</p>
 *
 * @param productName    the database product name (e.g. "PostgreSQL")
 * @param productVersion the database product version string
 * @param majorVersion   the major version number
 * @param minorVersion   the minor version number
 * @author RegalPine
 */
public record DatabaseMetadata(
        String productName,
        String productVersion,
        int majorVersion,
        int minorVersion
) {
}
