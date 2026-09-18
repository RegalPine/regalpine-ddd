package io.github.regalpine.ddd.infrastructure.mybatis.query.field;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Convenience factory methods for date/time {@link QueryField} types.
 *
 * <p>Phase XIII §5/§7: provides factory methods for Instant, LocalDate, LocalDateTime.</p>
 *
 * @author RegalPine
 */
public final class DateTimeField {

    private DateTimeField() {
    }

    public static QueryField<Instant> instant(String column) {
        return QueryFields.of(column, Instant.class);
    }

    public static QueryField<LocalDate> localDate(String column) {
        return QueryFields.of(column, LocalDate.class);
    }

    public static QueryField<LocalDateTime> localDateTime(String column) {
        return QueryFields.of(column, LocalDateTime.class);
    }
}
