package io.github.regalpine.ddd.infrastructure.mybatis.query;

/**
 * Defines a query's SQL and parameter binding for MyBatis execution.
 *
 * <p>Phase XII §5: encapsulates the SQL statement ID and parameter
 * type for a read-side query.</p>
 *
 * @param statementId the MyBatis mapped statement ID
 * @param parameterType the parameter class
 * @param resultType the result class
 * @author RegalPine
 */
public record MyBatisQueryDefinition(
        String statementId,
        Class<?> parameterType,
        Class<?> resultType
) {
}
