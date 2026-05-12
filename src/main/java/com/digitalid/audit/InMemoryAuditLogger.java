package com.digitalid.audit;

import com.digitalid.model.OrganisationType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class InMemoryAuditLogger implements AuditLogger {

    private final List<AuditEvent> events = new ArrayList<>();

    @Override
    public void log(OrganisationType organisationType, String operationName,
                    UUID digitalIdId, boolean success, String details) {
        events.add(new AuditEvent(organisationType, operationName, digitalIdId, success, details));
    }

    @Override
    public List<AuditEvent> getLog() {
        return Collections.unmodifiableList(events);
    }

    @Override
    public List<AuditEvent> getLogForId(UUID digitalIdId) {
        return Collections.unmodifiableList(
                events.stream()
                        .filter(e -> digitalIdId.equals(e.getDigitalIdId()))
                        .collect(Collectors.toList())
        );
    }
}