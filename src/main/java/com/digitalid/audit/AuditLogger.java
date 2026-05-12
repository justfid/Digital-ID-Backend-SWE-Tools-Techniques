package com.digitalid.audit;

import com.digitalid.model.OrganisationType;

import java.util.List;
import java.util.UUID;

/**
 * Records an immutable, append-only log of all significant operations performed on the platform.
 * Entries are written by both the management and consumption layers for every authorised or rejected request.
 */
public interface AuditLogger {

    /**
     * Appends a new event to the audit log.
     *
     * @param organisationType the organisation that performed the operation
     * @param operationName    the name of the operation, typically an {@link com.digitalid.auth.Operations} constant
     * @param digitalIdId      the UUID of the Digital ID involved, or {@code null} if no specific ID applies
     * @param success          {@code true} if the operation succeeded, {@code false} if it was rejected or failed
     * @param details          a human-readable description of the outcome or failure reason
     */
    void log(OrganisationType organisationType, String operationName,
             UUID digitalIdId, boolean success, String details);

    /**
     * Returns an unmodifiable view of all audit events in chronological order.
     *
     * @return an unmodifiable list of all {@link AuditEvent} entries
     */
    List<AuditEvent> getLog();

    /**
     * Returns an unmodifiable view of all audit events associated with a specific Digital ID.
     *
     * @param digitalIdId the UUID of the Digital ID to filter by
     * @return an unmodifiable list of {@link AuditEvent} entries matching the given id
     */
    List<AuditEvent> getLogForId(UUID digitalIdId);
}