package com.digitalid.consumption;

import com.digitalid.management.IdentityManager;
import com.digitalid.model.DigitalId;
import com.digitalid.model.DigitalIdStatus;

import java.time.LocalDate;
import java.util.UUID;

public class IdentityConsumptionServiceImpl implements IdentityConsumptionService {

    private final IdentityManager manager;

    public IdentityConsumptionServiceImpl(IdentityManager manager) {
        this.manager = manager;
    }

    @Override
    public VerificationResponse checkValidity(UUID id) {
        DigitalId digitalId;
        try {
            digitalId = manager.findById(id);
        } catch (IllegalArgumentException e) {
            return VerificationResponse.invalid("Identity not found");
        }

        if (digitalId.getStatus() == DigitalIdStatus.ACTIVE) {
            return VerificationResponse.valid("Identity is active");
        }
        if (digitalId.getStatus() == DigitalIdStatus.SUSPENDED) {
            return VerificationResponse.invalid("Identity is suspended");
        }
        return VerificationResponse.invalid("Identity is revoked");
    }

    @Override
    public VerificationResponse checkTaxEligibility(UUID id, LocalDate periodStart, LocalDate periodEnd) {
        DigitalId digitalId;
        try {
            digitalId = manager.findById(id);
        } catch (IllegalArgumentException e) {
            return VerificationResponse.invalid("Identity not found");
        }

        if (digitalId.getStatus() != DigitalIdStatus.ACTIVE) {
            return VerificationResponse.invalid("Identity is not active");
        }

        boolean wasSuspendedDuringPeriod = digitalId.getStatusHistory().stream()
                .anyMatch(entry -> entry.getStatus() == DigitalIdStatus.SUSPENDED
                        && !entry.getTimestamp().toLocalDate().isBefore(periodStart)
                        && !entry.getTimestamp().toLocalDate().isAfter(periodEnd));

        if (wasSuspendedDuringPeriod) {
            return VerificationResponse.invalid("Identity was suspended during the reporting period");
        }

        return VerificationResponse.valid("Identity is eligible for tax reporting");
    }

    @Override
    public VerificationResponse checkLicenceEligibility(UUID id) {
        DigitalId digitalId;
        try {
            digitalId = manager.findById(id);
        } catch (IllegalArgumentException e) {
            return VerificationResponse.invalid("Identity not found");
        }

        if (digitalId.getStatus() != DigitalIdStatus.ACTIVE) {
            return VerificationResponse.invalid("Identity is not active");
        }

        if (digitalId.isTemporaryRestriction()) {
            return VerificationResponse.invalid("Identity has a temporary restriction");
        }

        return VerificationResponse.valid("Identity is eligible for a driving licence");
    }
}