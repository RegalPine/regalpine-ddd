package io.github.regalpine.ddd.core.audit;

/**
 * Port for recording audit events.
 *
 * <p>Audit recording should not intrude into the domain model.
 * Infrastructure adapters implement this interface to persist
 * audit records to the appropriate store.</p>
 *
 * @author RegalPine
 */
public interface AuditRecorder {

    /**
     * Records an audit event.
     *
     * @param record the audit record, must not be {@code null}
     */
    void record(AuditRecord record);
}
