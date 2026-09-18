package io.github.regalpine.ddd.infrastructure.mybatis.repository;

/**
 * Defines the mapping between an aggregate type and its MyBatis mapper.
 *
 * <p>Phase XII §5: used during repository factory initialization to
 * register aggregate-to-mapper bindings.</p>
 *
 * @param aggregateType   the fully qualified aggregate class name
 * @param mapperNamespace the MyBatis mapper namespace
 * @param tableName       the database table name
 * @author RegalPine
 */
public record MyBatisRepositoryDefinition(
        String aggregateType,
        String mapperNamespace,
        String tableName
) {
}
