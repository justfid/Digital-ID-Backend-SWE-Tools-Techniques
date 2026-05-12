package com.digitalid.audit;

import com.digitalid.model.OrganisationType;

import java.util.List;
import java.util.UUID;

public interface AuditLogger {

    void log(OrganisationType organisationType, String operationName,
             UUID digitalIdId, boolean success, String details);

    List<AuditEvent> getLog();

    List<AuditEvent> getLogForId(UUID digitalIdId);
}