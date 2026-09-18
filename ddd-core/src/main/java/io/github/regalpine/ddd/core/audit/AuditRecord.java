package io.github.regalpine.ddd.core.audit;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Record capturing an auditable action within the system.
 *
 * @param principal     the acting principal
 * @param tenant        the tenant context
 * @param operation     the operation performed
 * @param resource      the resource affected
 * @param timestamp     when the operation occurred
 * @param result        the outcome (e.g. SUCCESS, FAILURE)
 * @param correlationId the correlation ID linking related operations
 * @author RegalPine
 */
public record AuditRecord(
        String principal,
        String tenant,
        String operation,
        String resource,
        Instant timestamp,
        String result,
        String correlationId
) {

    public AuditRecord {
        Objects.requireNonNull(operation, "operation must not be null");
        Objects.requireNonNull(resource, "resource must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
    }
}
