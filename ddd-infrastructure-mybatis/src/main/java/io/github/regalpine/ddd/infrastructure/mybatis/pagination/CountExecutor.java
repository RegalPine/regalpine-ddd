package io.github.regalpine.ddd.infrastructure.mybatis.pagination;

/**
 * Executes COUNT queries for pagination.
 *
 * <p>Phase XII §33: used by the query executor to determine
 * the total number of elements when {@code CountMode.EXACT} is active.</p>
 *
 * @author RegalPine
 */
public interface CountExecutor {

    /**
     * Executes a COUNT SQL query and returns the result.
     *
     * @param countSql  the COUNT SQL statement
     * @param parameter the query parameter
     * @return the count result
     */
    long count(String countSql, Object parameter);
}
