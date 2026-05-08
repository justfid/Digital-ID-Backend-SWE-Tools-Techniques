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
    public ValidityResponse checkValidity(UUID id) {
        DigitalId digitalId;
        try {
            digitalId = manager.findById(id);
        } catch (IllegalArgumentException e) {
            return new ValidityResponse(false, "Identity not found");
        }

        if (digitalId.getStatus() == DigitalIdStatus.ACTIVE) {
            return new ValidityResponse(true, "Identity is active");
        }
        if (digitalId.getStatus() == DigitalIdStatus.SUSPENDED) {
            return new ValidityResponse(false, "Identity is suspended");
        }
        return new ValidityResponse(false, "Identity is revoked");
    }

    @Override
    public TaxVerificationResponse checkTaxEligibility(UUID id, LocalDate periodStart, LocalDate periodEnd) {
        DigitalId digitalId;
        try {
            digitalId = manager.findById(id);
        } catch (IllegalArgumentException e) {
            return new TaxVerificationResponse(false, "Identity not found");
        }

        if (digitalId.getStatus() != DigitalIdStatus.ACTIVE) {
            return new TaxVerificationResponse(false, "Identity is not active");
        }

        boolean wasSuspendedDuringPeriod = digitalId.getStatusHistory().stream()
                .anyMatch(entry -> entry.getStatus() == DigitalIdStatus.SUSPENDED
                        && !entry.getTimestamp().toLocalDate().isBefore(periodStart)
                        && !entry.getTimestamp().toLocalDate().isAfter(periodEnd));

        if (wasSuspendedDuringPeriod) {
            return new TaxVerificationResponse(false, "Identity was suspended during the reporting period");
        }

        return new TaxVerificationResponse(true, "Identity is eligible for tax reporting");
    }

    @Override
    public LicenceVerificationResponse checkLicenceEligibility(UUID id) {
        DigitalId digitalId;
        try {
            digitalId = manager.findById(id);
        } catch (IllegalArgumentException e) {
            return new LicenceVerificationResponse(false, "Identity not found");
        }

        if (digitalId.getStatus() != DigitalIdStatus.ACTIVE) {
            return new LicenceVerificationResponse(false, "Identity is not active");
        }

        if (digitalId.isTemporaryRestriction()) {
            return new LicenceVerificationResponse(false, "Identity has a temporary restriction");
        }

        return new LicenceVerificationResponse(true, "Identity is eligible for a driving licence");
    }
}