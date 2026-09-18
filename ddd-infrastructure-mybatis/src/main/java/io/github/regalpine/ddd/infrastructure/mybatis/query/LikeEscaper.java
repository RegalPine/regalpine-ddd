package io.github.regalpine.ddd.infrastructure.mybatis.query;

/**
 * Escapes special LIKE wildcard characters in user input.
 *
 * <p>Phase XIII §17: user input containing {@code %}, {@code _}, or {@code \}
 * must be escaped before being used in a LIKE clause, so that these characters
 * are treated as literals rather than wildcards.</p>
 *
 * <p>Example: input {@code "100%"} is escaped to {@code "100\\%"}.</p>
 *
 * @author RegalPine
 */
public interface LikeEscaper {

    /**
     * Escapes LIKE special characters in the given value.
     *
     * @param value the raw user input
     * @return the escaped value safe for LIKE clauses
     */
    String escape(String value);
}
