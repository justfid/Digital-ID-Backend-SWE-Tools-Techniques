package com.digitalid.model;

import java.time.LocalDateTime;

public class StatusEntry {

    private final DigitalIdStatus status;
    private final LocalDateTime timestamp;
    private final String reason;

    public StatusEntry(DigitalIdStatus status, LocalDateTime timestamp, String reason) {
        this.status = status;
        this.timestamp = timestamp;
        this.reason = reason;
    }

    public DigitalIdStatus getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getReason() {
        return reason;
    }
}