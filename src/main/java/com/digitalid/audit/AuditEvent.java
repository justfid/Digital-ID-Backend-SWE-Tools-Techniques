package com.digitalid.audit;

import com.digitalid.model.OrganisationType;

import java.time.LocalDateTime;
import java.util.UUID;

public class AuditEvent {

    private final UUID eventId;
    private final OrganisationType organisationType;
    private final String operationName;
    private final UUID digitalIdId;
    private final boolean success;
    private final String details;
    private final LocalDateTime timestamp;

    public AuditEvent(OrganisationType organisationType, String operationName,
                      UUID digitalIdId, boolean success, String details) {
        this.eventId = UUID.randomUUID();
        this.organisationType = organisationType;
        this.operationName = operationName;
        this.digitalIdId = digitalIdId;
        this.success = success;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public UUID getEventId() {
        return eventId;
    }

    public OrganisationType getOrganisationType() {
        return organisationType;
    }

    public String getOperationName() {
        return operationName;
    }

    public UUID getDigitalIdId() {
        return digitalIdId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}