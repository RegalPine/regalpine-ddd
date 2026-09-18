package io.github.regalpine.ddd.infrastructure.mybatis.query;

/**
 * Default implementation of {@link LikeEscaper}.
 *
 * <p>Phase XIII §17: escapes {@code \}, {@code %}, and {@code _} characters
 * with a backslash prefix.</p>
 *
 * @author RegalPine
 */
public final class DefaultLikeEscaper implements LikeEscaper {

    @Override
    public String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
