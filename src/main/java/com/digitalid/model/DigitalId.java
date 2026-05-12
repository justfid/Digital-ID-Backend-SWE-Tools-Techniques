package com.digitalid.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DigitalId {

    private final UUID id;
    private final String nationalIdNumber;
    private final LocalDate dateOfBirth;
    private final String fullName;

    private String address;
    private String email;
    private boolean temporaryRestriction;

    private DigitalIdStatus status;
    private final List<StatusEntry> statusHistory;

    private DigitalId(Builder builder) {
        this.id = UUID.randomUUID();
        this.nationalIdNumber = builder.nationalIdNumber;
        this.dateOfBirth = builder.dateOfBirth;
        this.fullName = builder.fullName;
        this.address = builder.address;
        this.email = builder.email;
        this.temporaryRestriction = builder.temporaryRestriction;
        this.status = DigitalIdStatus.ACTIVE;
        this.statusHistory = new ArrayList<>();
        this.statusHistory.add(new StatusEntry(DigitalIdStatus.ACTIVE, LocalDateTime.now(), "Initial creation"));
    }

    public UUID getId() {
        return id;
    }

    public String getNationalIdNumber() {
        return nationalIdNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

    public boolean isTemporaryRestriction() {
        return temporaryRestriction;
    }

    public DigitalIdStatus getStatus() {
        return status;
    }

    public List<StatusEntry> getStatusHistory() {
        return Collections.unmodifiableList(statusHistory);
    }

    public DigitalIdMutator getMutator() {
        return new DigitalIdMutator(this);
    }

    void updateAddress(String address) {
        this.address = address;
    }

    void updateEmail(String email) {
        this.email = email;
    }

    void updateTemporaryRestriction(boolean temporaryRestriction) {
        this.temporaryRestriction = temporaryRestriction;
    }

    void updateStatus(DigitalIdStatus newStatus, String reason) {
        this.status = newStatus;
        this.statusHistory.add(new StatusEntry(newStatus, LocalDateTime.now(), reason));
    }

    public static class Builder {

        private final String nationalIdNumber;
        private final LocalDate dateOfBirth;
        private final String fullName;

        private String address;
        private String email;
        private boolean temporaryRestriction = false;

        public Builder(String nationalIdNumber, LocalDate dateOfBirth, String fullName) {
            this.nationalIdNumber = nationalIdNumber;
            this.dateOfBirth = dateOfBirth;
            this.fullName = fullName;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder temporaryRestriction(boolean temporaryRestriction) {
            this.temporaryRestriction = temporaryRestriction;
            return this;
        }

        public DigitalId build() {
            return new DigitalId(this);
        }
    }
}